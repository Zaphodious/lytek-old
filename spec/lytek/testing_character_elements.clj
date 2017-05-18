(ns lytek.testing-character-elements
  (:require [lytek.character.elements :as lyelm]
            [lytek.chron.merit :as lycmerit]
            [lytek.chron.elements :as lycelem]
            [lytek.chron.charm :as lyccharm]
            [lytek.col :as lycol]))

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
   (lycol/namemap
     [{:name           "single rank"
       :possible-ranks #{2}}
      {:name           "multiple ranks"
       :possible-ranks #{1 2 4}}
      {:name           "prereq athletics 3"
       :possible-ranks #{1 2 3 4 5}
       :prereq         [:or ["Athletics" 3]]}
      {:name           "multiple or prereqs"
       :possible-ranks #{1 2 3 4 5}
       :prereq         [:or ["Melee" 3] ["brawl" 3]]}
      {:name           "multiple and prereqs"
       :possible-ranks #{1 2 3 4 5}
       :prereq         [:and ["Melee" 3] [:brawl 3]]}
      {:name           "repurchasable"
       :possible-ranks #{1 2 3 4 5}
       :repurchasable  true}
      {:name           "adds sample tag"
       :possible-ranks #{1 2 3 4 5}
       :repurchasable  true
       :static-tags    [[:blorp]                            ;; For a given rank, adds the tags at index rank-1
                        [:blorp]
                        [:blorp]
                        [:blorp]
                        [:blorp]]}
      {:name           "adds tag from rank"
       :possible-ranks #{1 2 3 4 5}
       :repurchasable  true
       :static-tags    [[:primero]                          ;; For a given rank, adds the tags at index rank-1
                        [:segundo]
                        [:terciro]
                        [:fourth-y]
                        [:full-circle]]}
      {:name           "adds martial arts tag"
       :possible-ranks #{5}
       :static-tags    [[] [] [] []
                        [:martial-artist]]}
      {:name           "artifact tag giver"
       :possible-ranks #{2 3 4 5}
       :static-tags    [[]
                        [:artifact-2]
                        [:artifact-3]
                        [:artifact-4]
                        [:artifact-5]]}
      {:name           "hearthstone tag giver"
       :possible-ranks #{2 4}
       :static-tags    [[] [:hearthstone-2] [] [:hearthstone-4] []]}
      ])})


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
   (merge (lycol/namemap
            [{:name          "simple charm"
              :prereq-stats  [:athletics 1 1]
              :prereq-charms #{}}
             {:name          "higher-ability charm"
              :prereq-stats  ["athletics" 3 1]
              :prereq-charms #{}}
             {:name          "higher-essence charm"
              :prereq-stats  [:athletics 1 3]
              :prereq-charms #{}}
             {:name          "needs-one-prereq"
              :prereq-stats  [:athletics 1 1]
              :prereq-charms #{"simple charm"}}
             {:name         "gives a tag"
              :prereq-stats [:athletics 1 1]
              :static-tags  [:kafwunka]}
             {:name         "hobo style starter"
              :prereq-stats [:martial-arts 1 1]
              :style        "hobo style"}
             {:name         "awesome craft charm"
              :prereq-stats ["craft"  3 1]}])
          (make-bulk-charms "athleto-bulk" :athletics 1 1 20)
          (make-bulk-charms "performo-bulk" :performance 1 1 20))})

(def panoply-chron
  {:panoply
   (lycol/namemap
     [{:name "great and mighty sword"
       :type :artifact-3}
      {:name "totally rad shield"
       :type :artifact-4}
      {:name "gem of morbidity"
       :type :hearthstone-2}])
   })