(ns leap
  (:use arcadia.core)
  (:import [UnityEngine Time]))

; (.. cube transform)

(defcomponent Rotater [^float speed]
  (Update [this]
    (.. this transform (Rotate 0 (* speed Time/deltaTime) 0))))

(defn replace-component [^GameObject go ^Type ct]
  (if-let [c (get-component go ct)]
    (destroy c))
  (add-component go ct))