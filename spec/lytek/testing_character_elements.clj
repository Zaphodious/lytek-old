(ns lytek.testing-character-elements
  (:require [lytek.character.elements :as lyelm]
            [lytek.chron.merit :as lycmerit]
            [lytek.chron.elements :as lycelem]
            [lytek.chron.charm :as lyccharm]))

(def ability-rank-blocks
  {:all-nothing (zipmap lyelm/abilities (repeat 0))
   :all-low     (zipmap lyelm/abilities (repeat 1))
   :all-mid     (zipmap lyelm/abilities (repeat 3))
   :all-max     (zipmap lyelm/abilities (repeat 5))})

(def attribute-rank-blocks
  {:all-low (zipmap lyelm/attributes (repeat 1))
   :all-mid (zipmap lyelm/attributes (repeat 3))
   :all-max (zipmap lyelm/attributes (repeat 5))})

(def merit-chron
  {:merits
   {"single rank"          {:name           "single rank"
                            :possible-ranks #{2}}
    "multiple ranks"       {:name           "multiple ranks"
                            :possible-ranks #{1 2 4}}
    "prereq athletics 3"   {:name           "prereq athletics 3"
                            :possible-ranks #{1 2 3 4 5}
                            :prereq         [:or [:athletics 3]]}
    "multiple or prereqs"  {:name           "multiple or prereqs"
                            :possible-ranks #{1 2 3 4 5}
                            :prereq         [:or [:melee 3] [:brawl 3]]}
    "multiple and prereqs" {:name           "multiple  and prereqs"
                            :possible-ranks #{1 2 3 4 5}
                            :prereq         [:and [:melee 3] [:brawl 3]]}
    "repurchasable"        {:name           "multiple prereqs"
                            :possible-ranks #{1 2 3 4 5}
                            :repurchasable  true}}})


(defn make-bulk-charms [base-name attribute att-req ess-req number-charms]
  (into {} (map (fn [numbah] (let [charm-name (str base-name "-" (+ 1 numbah))]
                               {charm-name {:name          charm-name
                                            :prereq-stats  [attribute att-req ess-req]
                                            :prereq-charms #{}}}))
                (range number-charms))))

(defn make-bulk-charm-slots [base-name number-charms]
  (into [] (map (fn [numbah] (let [charm-name (str base-name "-" (+ 1 numbah))]
                               [charm-name (str "nothing to note " (+ 1 numbah))]))
                (range number-charms))))


(def charm-chron
  {:charms
   (merge {"simple charm"         {:name          "simple charm"
                                   :prereq-stats  [:athletics 1 1]
                                   :prereq-charms #{}}
           "higher-ability charm" {:name          "higher-ability charm"
                                   :prereq-stats  [:athletics 3 1]
                                   :prereq-charms #{}}
           "higher-essence charm" {:name          "higher-essence charm"
                                   :prereq-stats  [:athletics 1 3]
                                   :prereq-charms #{}}
           "needs-one-prereq"     {:name          "needs-one-prereq"
                                   :prereq-stats  [:athletics 1 1]
                                   :prereq-charms #{"simple charm"}}}
          (make-bulk-charms "athleto-bulk" :athletics 1 1 20)
          (make-bulk-charms "performo-bulk" :performance 1 1 20))})

