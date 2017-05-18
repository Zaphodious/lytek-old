(ns lytek.character
  (:require [lytek.character.elements :as elms]
            [lytek.core :refer :all]
            [clojure.spec.alpha :as s]
            [lytek.col :as lycol]
            [lytek.macros :as lymac]
            [clojure.string :as str]
            [com.rpl.specter :refer :all]))




(lymac/defrecord-with-default
  nascient-character
  "A character in the process of being created"
  {:chartype          :solar
   :caste             :dawn
   :favored-abilities #{}
   :caste-abiliites   #{}
   :attribute-ranks   (zipmap lytek.character.elements/attributes (repeat 1))
   :ability-ranks     (zipmap lytek.character.elements/abilities (repeat 0))
   :merits            []
   :charms            []
   :martial-arts      []
   :panoply           []})

(lymac/defrecord-with-default
  full-character
  "A character post character-creation. Contains a nascient-character with base information,
  and fields containing the differences since creation."
  {:initial-character default-nascient-character
   :diff-character    default-nascient-character})

(defn into-map [thing] (into {} thing))
(lymac/defmulti-using-map get-number-from-area
  "Gets a value from the provided character
  under the provided keyword."
  [character area-to-search what-to-find]
  area-to-search
  {:stuff        :and-fluff
   :merits       (-> character
                     (:merits)
                     (lycol/namemap)
                     (get what-to-find)
                     (:rank))
   :martial-arts (-> character
                     (:martial-arts)
                     (into-map)
                     (get what-to-find))
   :crafts       (-> character
                     (:crafts)
                     (into-map)
                     (get what-to-find))
   :default      (-> character area-to-search what-to-find)})

(defn keyword-from-string
  [label]
  (-> label
      (str/replace " " "-")
      (str/lower-case)
      (keyword)))

(defn contains-in-area?
  [character label area]
  (let [found-value (get-number-from-area character area label)]
    (if (-> found-value (nil?) (not))
      area
      nil)))

(defn where-in-character
  "Takes a character map and a String label, and returns a set of keys where this label can be found
  (either in keyword form or in literal String form). Keyword form location will always be first, if there
  is a conflict. If no location can be found, returns nil"
  [character label]
  (let [label-as-key (if (string? label) (keyword-from-string label) label)
        character-keywords (keys default-nascient-character)]
    (->> character-keywords
         (map (fn [area] (or (contains-in-area? character label area) (contains-in-area? character label-as-key area))))
         (filter #(not (nil? %)))
         (into #{})
         (lycol/nil-if-empty))))

(defn get-named-number
  "Gets a number from the character based on the name provided. Eg, \"Strength\"
  will return 4 if the character's :strength is 4, and \"Wings\" will return 3 if
  the character has the merit \"Wings\" with a ranking of 3.

  In two elements have the same name, attributes/abilities take priority, then
  merits, then charms, then it falls to the order that they are discovered
  as the function traverses the character map. To remove ambiguity, pass
  the keyword that the elemenet exists under as the third argument.

  If the requested character element is not found (either using discovery or
   under the provided keyword) the function returns 0."
  ([character element-name]
   (let [where (first (where-in-character character element-name))]
     (when where
       (get-named-number character element-name where))))
  ([character element-name location-keyword]
   (let [element-keyword (if (string? element-name) (keyword-from-string element-name) element-name)]
     (or (get-number-from-area character location-keyword element-name)
         (get-number-from-area character location-keyword element-keyword)))))

(defn passes-prereq [character element-name element-value]
  (let [namnumb (or (get-named-number character element-name) 0)]
    ;(println element-name namnumb)
    (>= namnumb element-value)))

(defn passes-prereqs [character prereq-vec]
  (let [passing-set (into #{} (map (fn [[elem-name elem-val]]
                                     (passes-prereq character elem-name elem-val))
                                   (rest prereq-vec)))]
    (case (first prereq-vec)
      :or (contains? passing-set true)
      :and (not (contains? passing-set false))
      "default")))

