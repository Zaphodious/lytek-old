(ns lytek.validation.creation-imp
  (:require [lytek.character :as lychar]
            [lytek.col :as lycol]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

;;;;;;;;;;
;;; Attribute Section
;;;;;;;;;;


(def att-cats
  {:physical #{:strength :dexterity :stamina}
   :social   #{:appearance :charisma :manipulation}
   :mental   #{:wits :intelligence :perception}})

(defn which-cat [att]
  (reduce-kv (fn [init k v] (if (contains? v att) k init)) nil att-cats))

(defn atts-into-cats [att-ranks]
  (reduce-kv (fn [r k v] (update r (which-cat k) #(assoc % k v)))
             {:mental {}, :social {}, :physical {}} att-ranks))



(defn rank-att-cats [atts-in-cats]
  (let [counted-cats (lycol/add-map-of-numbah-maps atts-in-cats)
        sorted-cats (reverse (lycol/sort-numbah-map counted-cats))
        just-cats (map (fn [[k v]] k) sorted-cats)]
    (into [] just-cats)))

(defn rank-atts-no-cats [att-ranks]
  (rank-att-cats (atts-into-cats att-ranks)))

(def att-cat-points-alloted [8 6 4])

(defn points-alloted-for-cats [vectored-cats]
  (zipmap vectored-cats att-cat-points-alloted))

(defn remaining-attribute-points
  [{atts :attribute-ranks chartype :chartype}]
  (let [ranked-atts (rank-atts-no-cats atts)
        max-cat-points (points-alloted-for-cats ranked-atts)
        current-cat-points (lycol/add-map-of-numbah-maps (atts-into-cats atts))

        diff-cat-points (map
                          (fn [k]
                            (let [da-max (k max-cat-points)
                                  da-current (k current-cat-points)
                                  diff (- da-max da-current -3)]
                              ;(println "key " k " max " da-max " current " da-current " diff " diff " atts " (k (atts-into-cats atts)))
                              [k diff]))
                          ranked-atts)]
    (into [] diff-cat-points)
    ))

(def bonus-points-per-att-cat [4 4 3])

(defn bonus-points-spent-on-attributes
  [{atts :attribute-ranks chartype :chartype :as character}]
  (let [att-points-remaining (remaining-attribute-points character)
        bonus-points-used-by-cat (map-indexed
                                   (fn [index [cat points]]
                                     (let [point-per (get bonus-points-per-att-cat index)
                                           this-point-total (* points point-per)
                                           proper-bonus-points (* -1 (min this-point-total 0))]
                                       [cat proper-bonus-points]))
                                   att-points-remaining)]
    (reduce (fn [total [_ numbah]] (+ numbah total)) 0 bonus-points-used-by-cat)
    ))

;;;;;;;;;;
;;; Solar-spcific Section
;;;;;;;;;;

(def solar-caste-abilities
  {:dawn     #{:archery :awareness :brawl :martial-arts :dodge :melee
               :resistance :thrown :war}
   :zenith   #{:athletics :integrity :performance :lore :presence
               :resistance :survival :war}
   :twilight #{:bureaucracy :craft :integrity :investigation
               :linguistics :lore :medicine :occult}
   :night    #{:athletics :awareness :dodge :investigation
               :larceny :ride :stealth :socialize}
   :eclipse  #{:bureaucracy :larceny :linguistics :occult
               :presence :ride :sail :socialize}})

(defn valid-solar-caste-ability? [ability caste]
  (contains? (caste solar-caste-abilities) ability))

(defn determine-invalid-caste-abs [{:keys [:caste-abiliites :caste]}]
  (reduce (fn [bad-abs ability]
            (if (valid-solar-caste-ability? ability caste)
              bad-abs
              (into bad-abs [ability])))
          []
          caste-abiliites))
(defn determine-invalid-favored-abs [{:keys [:favored-abilities :caste-abiliites]}]
  (reduce (fn [bad-abs ability]
            (if (contains? caste-abiliites ability)
              (into bad-abs [ability])
              bad-abs))
          []
          favored-abilities))

;;;;;;;;;;
;;; Ability Section
;;;;;;;;;;

(defn bonus-points-spent-on-abilities [character] true)

(defn what-bonus-vs-ability [number] {:ability-points (min 3 number) :bonus-points (max 0 (- number 3))})

(def max-ability-points 28)

(defn bonus-point-cost-for-ability [ability selected-abilities]
  (if (contains? selected-abilities ability) 1 2))

(defn points-vs-bonus-post-split [new-val remaining-points ability selected-abilities]
  (let [points-affordable-diff (min new-val (- remaining-points new-val))
        bonus-points-used (max 0 (* -1 points-affordable-diff))
        points-actually-affordable (min new-val (+ new-val points-affordable-diff))]
    ;(println "ability " ability " new rank " new-val " bonus " bonus-points-used)
    [(min new-val (+ new-val points-actually-affordable)) bonus-points-used]))

(defn rd-on-split-ability-list [selected-abilities]
  (fn [[running-points running-bonus] ability {:keys [ability-points bonus-points]}]
    (let [[points bonus] (points-vs-bonus-post-split ability-points
                                                     (- max-ability-points running-points)
                                                     ability
                                                     selected-abilities)]
      ;(println "ability " ability " for points " points " and bonus " bonus-points " with selected " selected-abilities)
      [(+ running-points points) (+ running-bonus
                                    (* (bonus-point-cost-for-ability ability selected-abilities)
                                       (+ bonus bonus-points)))])))

;;;;;;;;;;
;;; Merit Section
;;;;;;;;;;

(defn merit-points-allowed
  ([chartype] (merit-points-allowed chartype false))
  ([chartype experienced?] (if experienced? 13 10))
  )

(defn merit-check-prereqs-passed [errors-so-far [m-name m-rank] chron-merits character]
  (let [{:keys [:prereq] :as the-merit} (chron-merits m-name)]
    (if (not (lychar/passes-prereqs character prereq))
      (into errors-so-far [[[m-name m-rank] :prereq]])
      errors-so-far)))


(defn merit-check-rank-legal [errors-so-far [m-name m-rank] chron-merits]
  (let [{:keys [:possible-ranks] :as the-merit} (chron-merits m-name)]
    (if (not (contains? possible-ranks m-rank))
      (into errors-so-far [[[m-name m-rank] :possible-ranks]])
      errors-so-far)))

(defn get-error-for-merit [chron-merits character]
  (fn [errors-so-far merit-slot]
    (-> errors-so-far
        (merit-check-rank-legal merit-slot chron-merits)
        (merit-check-prereqs-passed merit-slot chron-merits character))))

(defn count-names [name-map name]
  (update
    name-map
    name
    inc))

(defn reduction-over-mappykins [errors-thus-far potentially-nil]
  (if (not (nil? potentially-nil))
    (conj errors-thus-far potentially-nil)))

(defn check-repurchasability [errors-so-far merit-slots chron-merits]
  (let [vec-of-names (into [] (map first merit-slots))
        times-name-appears (reduce count-names
                                   (zipmap vec-of-names (repeat 0))
                                   vec-of-names)
        names-appearing-multiple (reduce-kv (fn [the-names name times]
                                              (if (< 1 times) (conj the-names name) the-names))
                                            []
                                            times-name-appears)
        error-merits (->> names-appearing-multiple
                          (map (fn [m-name] (when (not (:repurchasable (chron-merits m-name)))
                                              [[m-name 0] :repurchasable])))
                          (reduce reduction-over-mappykins errors-so-far))]
    error-merits))

(defn determine-merit-errors [{:keys [:merits :ability-ranks :chartype :caste]
                               :as   character} chron-merits]
  (let [individual-errors (reduce (get-error-for-merit chron-merits character) [] merits)
        set-errors (-> []
                       (check-repurchasability merits chron-merits))]
    (into set-errors individual-errors)))

;;;;;;;;;;
;;; Charm Section
;;;;;;;;;;


(defn charm-points-allowed [chartype]
  15)

(defn charm-check-essence-req-met [errors-so-far {:keys [:supernal] :as character}
                                   {[ability-req _ essence-req] :prereq-stats chname :name}]
  (if (or (= 1 essence-req) (= ability-req supernal))
    errors-so-far
    (into errors-so-far [[chname :supernal]])))

(defn charm-check-ability-req-met [errors-so-far {:keys [:supernal :ability-ranks] :as character}
                                   charm-slot
                                   {[ability-req ability-val-req] :prereq-stats chname :name}]
  (if (lychar/passes-prereq character ability-req ability-val-req)
    errors-so-far
    (into errors-so-far [[(first charm-slot) :prereq-stats]])))

(defn charm-check-stats-legal [errors-so-far charm-slot chron-charms character]
  (let [the-charm (chron-charms (first charm-slot))]
    (-> errors-so-far
        (charm-check-essence-req-met character the-charm)
        (charm-check-ability-req-met character charm-slot the-charm))))



(defn charm-check-prereqs-present [errors-so-far [charm-name charm-desc :as charm-slot] chron-charms {:keys [:charms] :as character}]
  (let [{:keys [:prereq-charms] :as actual-charm} (get chron-charms (first charm-slot))
        split-charms (split-with (fn [[name-o]] (not (= name-o charm-name))) charms)
        preceeding-charms (into #{} (first split-charms))]
    ;(println preceeding-charms)
    (if (or (empty? prereq-charms)
            (and (not (empty? preceeding-charms))
                 (not (empty? (clojure.set/intersection preceeding-charms prereq-charms)))))
      (do                                                   ;(println "prereqs for " charm-name " are " prereq-charms " and preceeding " preceeding-charms)
        errors-so-far)
      (do                                                   ;(println "charms are " preceeding-charms ", prereq charms for " charm-name " are " prereq-charms "result is " (preceeding-charms charm-name))
        (conj errors-so-far [charm-name :prereq-charms])))))

(defn charm-check-prereqs-present [errors-so-far [charm-name charm-desc :as charm-slot] chron-charms {:keys [:charms] :as character}]
  (let [{:keys [:prereq-charms] :as actual-charm} (get chron-charms charm-name)
        prereq-charms-has? (not (empty? prereq-charms))
        index-of-actual-charm (lycol/first-index #{charm-slot} charms)
        indexes-of-prereqs (when prereq-charms-has?
                             (map (fn [thing-name] (lycol/first-index #(= thing-name (first %)) charms)) prereq-charms))
        an-index-is-greater (when prereq-charms-has?
                              (reduce
                                (fn [yeah? inx] (if yeah?
                                                  yeah?
                                                  (> inx index-of-actual-charm)
                                                  ))
                                false
                                indexes-of-prereqs))]
    (if an-index-is-greater
      (conj errors-so-far [charm-name :prereq-charms])
      errors-so-far)))

(defn get-error-for-charm [chron-charms character]
  (fn [errors-so-far charm-slot]
    (-> errors-so-far
        (charm-check-stats-legal charm-slot chron-charms character)
        (charm-check-prereqs-present charm-slot chron-charms character))))

(defn is-selected-ability-charm? [{:keys [caste-abilities favored-abilities]} chron-charms charm]
  (let [charm-name (first charm)
        chron-charm (get chron-charms charm-name)

        charm-ability (first (:prereq-stats chron-charm))
        ability-is-in-set (contains? (into caste-abilities favored-abilities) charm-ability)]
    ability-is-in-set))

(defn names-of-selected-charms [{:keys [caste-abilities favored-abilities charms] :as character} chron-charms]
  (let [selected-charms (filter #(is-selected-ability-charm? character chron-charms %) charms)
        charm-names (map first selected-charms)
        ]
    (into #{} charm-names)))

(defn filter-charms-by-refactored [{:keys [caste-abilities favored-abilities charms] :as character} chron-charms]
  (lycol/filter-with-not #(is-selected-ability-charm? character chron-charms %)
                         charms))

