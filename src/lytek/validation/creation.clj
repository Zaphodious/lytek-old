(ns lytek.validation.creation
  (:require [lytek.character.elements :as lyelm]
            [lytek.col :as lc]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

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
  (let [counted-cats (lc/add-map-of-numbah-maps atts-in-cats)
        sorted-cats (reverse (lc/sort-numbah-map counted-cats))
        just-cats (map (fn [[k v]] k) sorted-cats)]
    (into [] just-cats)))

(defn rank-atts-no-cats [att-ranks]
  (rank-att-cats (atts-into-cats att-ranks)))

(def att-cat-points-alloted [8 6 4])

(defn points-alloted-for-cats [vectored-cats]
  (zipmap vectored-cats att-cat-points-alloted))

(defn- remaining-attribute-points
  [{atts ::lyelm/attribute-ranks chartype ::lyelm/chartype}]
  (let [ranked-atts (rank-atts-no-cats atts)
        max-cat-points (points-alloted-for-cats ranked-atts)
        current-cat-points (lc/add-map-of-numbah-maps (atts-into-cats atts))

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

(defn- bonus-points-spent-on-attributes
  [{atts ::lyelm/attribute-ranks chartype ::lyelm/chartype :as character}]
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

(defn attribute-info
  [character]
  (let [att-points-remaining (into {} (remaining-attribute-points character))
        cleaned-up-att-points-remaining (into {}
                                              (map (fn [[k v]] [k (max 0 v)])
                                                   att-points-remaining))
        bonus-points-spent (bonus-points-spent-on-attributes character)
        ]
    {:bonus-points-spent         bonus-points-spent
     :attribute-points-remaining cleaned-up-att-points-remaining}))



(comment (println (remaining-attribute-points
                    {::lyelm/chartype :solar
                     ::lyelm/attribute-ranks
                                      {:strength   5 :dexterity 5 :stamina 1
                                       :appearance 3 :charisma 3 :manipulation 3
                                       :wits       3 :intelligence 2 :perception 2}})))

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

(defn ability-info
  [{:keys [::lyelm/ability-ranks ::lyelm/chartype ::lyelm/caste ::lyelm/favored-abilities ::lyelm/caste-abiliites]
    :as   character}]
  (let [split-ranks (into {} (map (fn [[k v]] [k (what-bonus-vs-ability v)]) ability-ranks))
        [points bonus] (reduce-kv (rd-on-split-ability-list (into favored-abilities caste-abiliites))
                                  [0 0]
                                  split-ranks)]
    {:bonus-points-spent (+ bonus), :ability-points-remaining (- max-ability-points points)}))

