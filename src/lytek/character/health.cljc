(ns lytek.character.health)

(defn vec-o [numbas-repeated thing]
  (into [] (take numbas-repeated (repeat thing))))

(defn get-health-vector
  [{:keys                                          [levels]
    {:keys [bashing lethal aggrivated] :as damage} :damage
    :as                                            arg}]
  (let [total-levels (reduce-kv #(+ %1 %3) 0 levels)
        levels-vec (conj (reduce-kv (fn [vec-so-far level-rank level-amount]
                                      (into vec-so-far (take level-amount (repeat
                                                                            (if (pos-int? level-rank)
                                                                              (- level-rank)
                                                                              level-rank))))) [] levels)
                         :inc)
        damage-vec (-> []
                       (into (take bashing (repeat :bashing)))
                       (into (take lethal (repeat :lethal)))
                       (into (take aggrivated (repeat :aggrivated)))
                       (into (take (+ 1 (- total-levels bashing lethal aggrivated)) (repeat :empty))))]
    (into [] (map vector levels-vec damage-vec))))