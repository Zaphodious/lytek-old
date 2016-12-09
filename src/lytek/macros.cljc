(ns lytek.macros)

(defmacro dbg [x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(defmacro nil?alt
  "returns the alternate if the primary is nil"
  [primary alternate]
  (if (nil? primary) alternate primary))

(defmacro try-nil
  "returns nil if there is an exception"
  [body]
  `(try ~body (catch ~'Exception ~'e (do (comment (println ~'e)) nil))))

(defn typeassoc
  [fields fieldmap]
  ((into [] (map #(with-meta % {:tag (type ((keyword (str %)) fieldmap))}) fields))))

(defmacro defrecord-with-default
  [thingname docstring bodymap & body]
  (let [make-name (str "map->" thingname "-with-default")
        using-fields (fn [& maps] (reduce into maps))
        make-default-name (str "default-" thingname)
        mk-op (fn [bodylist] (replace {:using-fields using-fields} bodylist))
        evaluated-bodymap (if (list? bodymap) (eval (mk-op bodymap)) (if (map? bodymap) bodymap {}))
        options (if (map? (first body)) (first body) {})
        fields (into [] (map #(symbol (str (name %))) (keys evaluated-bodymap))) ; Get symbols by way of translating keywords into strings)
        fields-with-meta (into [] (map #(with-meta % {:tag (type ((keyword (str %)) evaluated-bodymap))}) fields))
        ;field-with-meta (typeassoc fields evaluated-bodymap)
        ]
    `(do
       ;~(println "types of " thingname)
       ;~(map #(if (println (meta %)) 0 'println) (if (:type-hint options) fields-with-meta fields))
       (defrecord
         ~thingname
         ^{:doc ~docstring}
         ~(if (:type-hint options) fields-with-meta fields)
         ~@(nil?alt (if (empty? options) body (rest body)) nil))
       (defn ~(symbol make-name)
         [values#] (~(symbol (str "map->" thingname)) (merge ~evaluated-bodymap values#)))

       (def ~(symbol make-default-name)
         (~(symbol make-name) (~(symbol (str "map->" thingname)) ~bodymap)))
       (println (str "\nDefault for " ~thingname))
       (println ~(symbol make-default-name))
       (println ""))))

(defn map-kv
  "Given a map and a function of two arguments, returns a list
  resulting from applying the function to each of its entries.

  Basically just actual clojure.core/map with the arguments pre-destructured."
  [f m]
  (map (fn [[k v]] (f k v)) m))

(defmacro defmethod-using-map
  [thingname params map-of-expressions]
  (conj (map-kv (fn [match-statement fun]
                  `(defmethod ~thingname ~match-statement
                     ~params
                     (try-nil ~fun))
                  ) map-of-expressions) `do))

(defmacro defmulti-using-map
  "Allows the declaration of a multimethod and a set of implimentations.

  Sample usage (not good usage, just simple):
  (defmulti-using-map maybe-multiply
            \"this is a docstring\"
            [param1 another-param] (> 0 param1)
                      {true  (/ param1 another-param)
                       false (* param1 another-param)})"
  [thingname docstring params dispatch-form map-of-expressions]
  `(do
     (defmulti ^{:doc ~docstring} ~thingname (fn ~params (try-nil ~dispatch-form)))
     (defmethod-using-map ~thingname ~params ~map-of-expressions)))

(defn- using-fields
  [& maps]
  (reduce into maps))