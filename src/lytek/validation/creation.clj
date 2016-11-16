(ns lytek.validation.creation
  (:require [lytek.col :as lc]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [lytek.validation.creation-imp :refer :all]
            [com.rpl.specter :refer :all]
            [lytek.macros :as lymac]))



(defn validate-attributes
  [character]
  (let [att-points-remaining (into {} (remaining-attribute-points character))
        cleaned-up-att-points-remaining (into {}
                                              (map (fn [[k v]] [k (max 0 v)])
                                                   att-points-remaining))
        bonus-points-spent (bonus-points-spent-on-attributes character)
        ]
    {:bonus-points-spent         bonus-points-spent
     :attribute-points-remaining cleaned-up-att-points-remaining}))


(defn validate-exalt-spcific [{:keys [:favored-abilities :caste-abiliites :supernal] :as character}]
  (let [invalid-caste-abs (determine-invalid-caste-abs character)
        invalid-favored-abs (determine-invalid-favored-abs character)
        has-caste-ab-errors (not (empty? invalid-caste-abs))
        has-favored-ab-errors (not (empty? invalid-favored-abs))
        supernal-in-selected-caste-abs (contains? caste-abiliites supernal)
        supernal-is-in-error (and (not supernal-in-selected-caste-abs) (not (nil? supernal)))]
    (if-not (or has-caste-ab-errors has-favored-ab-errors supernal-is-in-error)
      {:caste-abilities-remaining   (- 5 (count caste-abiliites))
       :favored-abilities-remaining (- 5 (count favored-abilities))
       :supernal-needs-selecting?   (nil? supernal)}
      (lc/scrub-nulls-from-vector
        [(when has-caste-ab-errors [invalid-caste-abs :caste-abiliites])
         (when has-favored-ab-errors [invalid-favored-abs :favored-abilities])
         (when supernal-is-in-error [supernal :supernal])]))))


(defn validate-abilities
  [{:keys [:ability-ranks :chartype :caste :favored-abilities :caste-abiliites]
    :as   character}]
  (let [split-ranks (into {} (map (fn [[k v]] [k (what-bonus-vs-ability v)]) ability-ranks))
        [points bonus] (reduce-kv (rd-on-split-ability-list (into favored-abilities caste-abiliites))
                                  [0 0]
                                  split-ranks)]
    {:bonus-points-spent (+ bonus), :ability-points-remaining (- max-ability-points points)}))


(defn validate-merits [{:keys [:merits :ability-ranks :chartype :caste] :as character}
                       {chron-merits :merits :as chron}]
  (let [merit-point-total (->> merits
                               (map #(second %))
                               (reduce +))
        errors (determine-merit-errors character chron-merits)]
    (if (empty? errors) {:merit-points-remaining (max 0 (- (merit-points-allowed chartype) merit-point-total))
                         :bonus-points-spent     (max 0 (- merit-point-total (merit-points-allowed chartype)))}
                        errors)))


(defn validate-charms [{:keys [:charms :chartype :caste :caste-abiliites :favored-abilities] :as character}
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
       :bonus-points-spent     bonus-points-used}
      individual-errors)))

(defn get-static-character-tags [character chron]
  (->> (keys character)
       (map #(get-static-tags-in % character chron))
       (reduce into [])
       (reduce into [])))