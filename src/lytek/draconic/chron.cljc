(ns lytek.draconic.chron
  (:require [draconic.ui :as ui]
            [lytek.draconic.chron.merit :as dcm]
            [clojure.string :as str]
            [draconic.ui :as ui]
            [clojure.string :as str]
            [clojure.string :as str]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.edn :as edn]))
(comment chronEditorContent
         meritDescriptionText
         rankCheck1
         meritAndToggle
         meritPrereqText
         meritTagText1
         )
(defn de-mystify-prereqs [[_ & prereq-vecs]]
  (str/replace-first (->> prereq-vecs
                          (reduce (fn [building [elementy rank]]
                                    (do (println elementy " at " rank)
                                        (str building ", " (-> elementy
                                                               (name)
                                                               (str/capitalize)) " " rank)))
                                  ""
                                  ))
                     ", " ""))
(defn keyvec-as-string [keyvec]
  (-> keyvec
      str
      (str/replace "[" "")
      (str/replace "]" "")
      ))
(defn string-as-keyvek [stringer]
  (-> (str "[" stringer "]")
      edn/read-string))

(defn remystify-prereq-string [prereq-string]
  (->> (-> prereq-string
           (str/replace "," "\n")
           str/split-lines)
       (filter #(not (empty? %)))))



(defn put-merit-into-editor!
  [{:keys [name possible-ranks prereq static-tags description] :as merit-map}
   nodemap]
  (let [[tags1 tags2 tags3 tags4 tags5] static-tags
        valof (fn [thingy] (get nodemap thingy))]
    (run! (fn [[anode thingy]]
          (println (str "doing " anode "now"))
          (ui/set-state! anode thingy))
        [[(valof dcm/nameField) name]
         [(valof dcm/descritpion) description]
         [(valof dcm/rank1) (contains? possible-ranks 1)]
         [(valof dcm/rank2) (contains? possible-ranks 2)]
         [(valof dcm/rank3) (contains? possible-ranks 3)]
         [(valof dcm/rank4) (contains? possible-ranks 4)]
         [(valof dcm/rank5) (contains? possible-ranks 5)]
         [(valof dcm/andTog) (= :and (first prereq))]
         [(valof dcm/orTog) (= :or (first prereq))]
         [(valof dcm/prereq) (de-mystify-prereqs prereq)]
         [(valof dcm/tag1) (keyvec-as-string tags1)]
         [(valof dcm/tag2) (keyvec-as-string tags2)]
         [(valof dcm/tag3) (keyvec-as-string tags3)]
         [(valof dcm/tag4) (keyvec-as-string tags4)]
         [(valof dcm/tag5) (keyvec-as-string tags5)]
         ]))

  )

(defn scrape-merit-from-editor [nodemap]
  (let [valmap (into {} (ui/get-state-from-all (vals (select-keys nodemap dcm/fields))))
        valof (fn [thingy] (get valmap thingy))]
    {:name           (valof dcm/nameField)
     :possible-ranks (->> [(when (valof dcm/rank1) 1)
                           (when (valof dcm/rank2) 2)
                           (when (valof dcm/rank3) 3)
                           (when (valof dcm/rank4) 4)
                           (when (valof dcm/rank5) 5)
                           ]
                          (filter #(not (nil? %)))
                          (into #{}))
     :prereq         (into [(if (valof dcm/andTog) :and :or)]
                           (filter #(not (empty? %))
                                   (->> (-> (valof dcm/prereq)
                                            (str/replace "," "\n")
                                            str/split-lines)
                                        (map str/trim)
                                        (map #(str/split % #" "))
                                        (map (fn [[namo numbstr]] [namo (edn/read-string numbstr)]))))
                           )
     :static-tags    [(string-as-keyvek (valof dcm/tag1))
                      (string-as-keyvek (valof dcm/tag2))
                      (string-as-keyvek (valof dcm/tag3))
                      (string-as-keyvek (valof dcm/tag4))
                      (string-as-keyvek (valof dcm/tag5))]
     :description    (valof dcm/descritpion)})
  )

