(ns lytek.character.elements
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(def attributes #{:strength :dexterity :stamina :appearance :charisma :manipulation :wits :intelligence :perception})
(def abilities #{:archery :athletics :awareness :brawl :bureaucracy :dodge :integrity :investigation :larceny
                 :linguistics :lore :medicine :melee :occult :performance :presence :resistance :ride
                 :sail :socialize :stealth :survival :thrown :war})

(s/def ::attribute attributes)
(s/def ::ability abilities)

(s/def ::attribute-ranks (s/map-of ::attribute (s/int-in 1 6) :count (count attributes)
                                   :gen #(gen/fmap
                                           (fn [numbas] (zipmap attributes numbas))
                                           (s/gen (s/every (s/int-in 1 6) :count (count attributes))))))
(s/def ::ability-ranks (s/map-of ::ability (s/int-in 1 6) :count (count abilities)
                                 :gen #(gen/fmap
                                         (fn [numbas] (zipmap abilities numbas))
                                         (s/gen (s/every (s/int-in 0 6) :count (count abilities))))))

(def character-fields-with-combinor-fn
  {:chartype          (fn [orig diff] (:chartype diff))
   :caste             (fn [orig diff] (:caste diff))
   :favored-abilities (fn [orig diff] (into (:favored-abilities orig) (:favored-abilities diff)))
   })                                                       ; TO-DO: Finish writing diff fns