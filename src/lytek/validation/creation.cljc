(ns lytek.validation.creation
  (:require [lytek.col :as lc]
            [lytek.validation.creation-imp :refer :all]
            [com.rpl.specter :as sp :refer :all]
            [lytek.macros :as lymac]))

(defn get-static-character-tags [character chron]
  (->> (keys character)
       (map #(get-static-tags-in % character chron))
       (reduce into [])
       (reduce into [])))

(defn validate-attributes
  [character chron]
  (let [att-points-remaining (into {} (remaining-attribute-points character))
        cleaned-up-att-points-remaining (into {}
                                              (map (fn [[k v]] [k (max 0 v)])
                                                   att-points-remaining))
        bonus-points-spent (bonus-points-spent-on-attributes character)
        ]
    {:bonus-points-spent         bonus-points-spent
     :attribute-points-remaining cleaned-up-att-points-remaining
     :export-ready               (= 0 (reduce-kv (fn [v k r] (+ v r)) 0 cleaned-up-att-points-remaining))}))


(defn validate-exalt-spcific [{:keys [:favored-abilities caste-abilities :supernal] :as character} chron]
  (let [invalid-caste-abs (determine-invalid-caste-abs character)
        invalid-favored-abs (determine-invalid-favored-abs character)
        has-caste-ab-errors (not (empty? invalid-caste-abs))
        has-favored-ab-errors (not (empty? invalid-favored-abs))
        supernal-in-selected-caste-abs (contains? caste-abilities supernal)
        supernal-is-in-error (and (not supernal-in-selected-caste-abs) (not (nil? supernal)))]
    (if-not (or has-caste-ab-errors has-favored-ab-errors supernal-is-in-error)
      (let [car (- 5 (count caste-abilities))
            far (- 5 (count favored-abilities))
            sns (nil? supernal)]
        {:caste-abilities-remaining   car
         :favored-abilities-remaining far
         :supernal-needs-selecting?   sns
         :export-ready                (and (= 0 (+ car far))
                                           (not sns))})
      (lc/scrub-nulls-from-vector
        [(when has-caste-ab-errors [invalid-caste-abs :caste-abilities])
         (when has-favored-ab-errors [invalid-favored-abs :favored-abilities])
         (when supernal-is-in-error [supernal :supernal])]))))



(defn validate-abilities [{:keys [:ability-ranks :chartype :caste :favored-abilities caste-abilities :martial-arts :crafts]
                           :as   character}
                          chron]
  (let [ability-errors (if (not (empty? martial-arts))
                         (if (not (contains? (into #{} (get-static-character-tags character chron)) :martial-artist))
                           [:static-tags :martial-artist]))
        split-ranks (->> ability-ranks
                         (split-ability-ranks)
                         (uncrack-alt-abilities :brawl martial-arts)
                         (uncrack-alt-abilities :craft crafts))
        selected-abilities (into favored-abilities caste-abilities)
        [points bonus] (reduce-kv (rd-on-split-ability-list selected-abilities chartype) [0 0] split-ranks)]
    (if ability-errors ability-errors
                       (let [points-remaining (- (max-ability-points chartype) points)]
                         {:bonus-points-spent (+ bonus), :ability-points-remaining points-remaining, :export-ready (= points-remaining 0)})))
  )

(defn validate-merits [{:keys [:merits :ability-ranks :chartype :caste] :as character}
                       {chron-merits :merits :as chron}]
  (let [merit-point-total (->> merits
                               (map #(second %))
                               (reduce +))
        mpr (max 0 (- (merit-points-allowed chartype) merit-point-total))
        bps (max 0 (- merit-point-total (merit-points-allowed chartype)))
        errors (determine-merit-errors character chron-merits)]
    (if (empty? errors) {:merit-points-remaining mpr
                         :bonus-points-spent     bps
                         :export-ready           (= 0 mpr)}
                        errors)))


(defn validate-charms [{:keys [:charms :chartype :caste caste-abilities :favored-abilities] :as character}
                       {chron-merits :merits chron-charms :charms :as chron}]
  (let [individual-errors (reduce (get-error-for-charm chron-charms character) [] charms)
        selected-charms-names (names-of-selected-charms character chron-charms)
        number-charms-beyond-allowed (max 0 (- (count charms) (charm-points-allowed chartype)))
        just-beyond-regular-charms (take-last number-charms-beyond-allowed charms)
        points-remaining (max 0 (- (charm-points-allowed chartype) (count charms)))
        bonus-points-used (if (= points-remaining 0)
                            (->> just-beyond-regular-charms
                                 (map #(if (contains? selected-charms-names (first %)) 1 2))
                                 (reduce +))
                            0)]
    (if (empty? individual-errors)
      {:charm-points-remaining points-remaining
       :bonus-points-spent     bonus-points-used
       :export-ready           (= 0 points-remaining)}
      individual-errors)))

(defn validate-panoply [{:keys [:charms :chartype :caste caste-abilities :favored-abilities :panoply] :as character}
                        {chron-merits :merits chron-charms :charms chron-panoply :panoply :as chron}]
  (let [artifact-tags [:artifact-2 :artifact-3 :artifact-4 :artifact-5 :hearthstone-2 :hearthstone-4]
        replaced-inventory (transform [ALL (if-path [:type :artifact] [STAY])]
                                      (fn [{:keys [name]}] (get chron-panoply name)) panoply)
        count-inv-of-type (fn [the-type] {the-type (count (select [ALL (if-path [:type #{the-type}] [STAY])] replaced-inventory))})
        count-tags-of-type (fn [the-type] {the-type (count (select [ALL #{the-type}] (get-static-character-tags character chron)))})
        types-in-inventory (into {} (map count-inv-of-type artifact-tags))
        tags-in-character (into {} (map count-tags-of-type artifact-tags))
        remaining-things (into {} (map (fn [[k1 a] [k2 b]] [k2 (- b a)]) types-in-inventory tags-in-character))
        things-errors (reduce-kv (fn [error-vec k v]
                                   (if (> 0 v) (conj error-vec [:static-tags k])
                                               error-vec)) []
                                 remaining-things)]
    (if (empty? things-errors) {:remaining-artifacts remaining-things,
                                :export-ready        (= 0
                                                        (reduce-kv (fn [v k r] (+ v r)) 0 remaining-things))}
                               things-errors)))

(defn apply-solar-validators
  "Applies validation functions for Solar Exalts, returning a map of :area to {validation result}"
  [character chron] (sp/transform
                      [ALL sp/LAST]
                      (fn [t] (t character chron))
                      {:abilities     validate-abilities
                       :attributes    validate-attributes
                       :charms        validate-charms
                       :exalt-spcific validate-exalt-spcific
                       :merits        validate-merits
                       :panoply       validate-panoply
                       }))

(defn can-be-exported?
  ([the-char the-chron] (can-be-exported? the-char the-chron apply-solar-validators))
  ([the-char the-chron validator]
   (let [validation-results (validator the-char the-chron)
         bonus-points (reduce +
                              (select
                                [ALL
                                 LAST
                                 (sp/keypath :bonus-points-spent)
                                 (sp/pred number?)]
                                validation-results))
         categories-not-ready (->> (filter #(not (:export-ready (last %))) validation-results)
                                   (select [ALL FIRST]))
         total-errors (into categories-not-ready
                            (if (= bonus-points 15)
                              []
                              [:bonus-points-spent]))
         ready? (empty? total-errors)]
     {:export-ready ready? :not-ready total-errors :bonus-points-spent bonus-points :full-result validation-results}
     )))