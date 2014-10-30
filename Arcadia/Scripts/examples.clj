(ns examples
  (:use unity.core)
  (:import [UnityEngine Color]))

;; http://docs.unity3d.com/ScriptReference/MonoBehaviour.OnMouseEnter.html
(defcomponent ExampleClassA []
  (OnMouseEnter [this]
    (set! (.. this renderer material color)
          Color/red)))

;; http://docs.unity3d.com/ScriptReference/MonoBehaviour.Update.html
(defcomponent ExampleClassB []
  (Update [this]
    (.. this transform (Translate 0 0 (* Time/deltaTime 1)))))

;; http://docs.unity3d.com/ScriptReference/MonoBehaviour.Update.html
(defcomponent ExampleClassC []
  (Update [this]
    (.. this transform (Translate 0 0 (* Time/deltaTime 1)))))

;; http://docs.unity3d.com/ScriptReference/MonoBehaviour.OnCollisionEnter2D.html
(defcomponent ExampleClassD []
  (OnCollisionEnter2D [this coll]
    (if (= "Enemy" (.. coll gameObject tag))
      (.. coll gameObject (SendMessage "ApplyDamage" 10)))))

;; http://docs.unity3d.com/ScriptReference/MonoBehaviour.OnCollisionStay2D.html
(defcomponent ExampleClassE [^float rechargeRate
                             ^float batteryLevel]
  (OnCollisionStay2D [this coll]
    (if (= "RechargePoint" (.. coll gameObject tag))
      (set! batteryLevel (float (min 100 (+ batteryLevel (* rechargeRate Time/deltaTime))))))))