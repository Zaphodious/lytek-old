(ns lytek.character
  (:require [lytek.character.elements :as elms]
            [lytek.core :refer :all]
            [clojure.spec :as s]))



(def example-solar
#:elms{:name "Ice-Laden Sword Maiden"
       :player "Alex"
       :type :solar
       :caste :zenith
       :attributes (into {} (map #(vector % 1) elms/attributes))
       :attribute-category-ranks [:social :mental :physical]
       :attribute-points-spent {:physical 0 :social 0 :mental 0}
       :abilities (into {} (map #(vector % 0) elms/abilities))
       :abilities-favored #{}
       :abilities-caste #{}
       :ability-supernal :athletics
       :bonus-points-spent 0})