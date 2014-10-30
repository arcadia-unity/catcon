(ns ship
  (:use arcadia.core)
  (:import [UnityEngine Debug Vector3 Time]))

(defn ship-update [this]
  (let [transform (.. this transform)
        speed (.speed this)]
    (.Translate transform
                (Vector3/op_Multiply
                  (.forward transform)
                  (* speed Time/deltaTime)))
    (.Rotate transform (* speed Time/deltaTime) 0 0)))

(defcomponent ship [^float speed]
  ; use this for initialization
  (Start [this])
  
  ; update is called once per frame
  (Update [this]
    (ship-update this)))