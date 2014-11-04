(ns catcon.camera
  (:use arcadia.core
        catcon.linear)
  (:import [UnityEngine Debug Input Vector3 Space]))

(defcomponent Camera [^float speed
                      ^Vector3 target
                      ^Vector3 focus]
  (Update [this]
    (let [towards-target (v* 0.1 (v- target focus))]
      (set! (.focus this) (v+ focus towards-target))
      (doto (.transform this)
        (.Translate towards-target Space/World)
        (.RotateAround focus Vector3/up (* speed (Input/GetAxis "Horizontal")))
        (.Translate 0 (* speed (Input/GetAxis "Vertical")) 0)
        (.LookAt focus)))))