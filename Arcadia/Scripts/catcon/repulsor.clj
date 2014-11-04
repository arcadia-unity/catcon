(ns catcon.repulsor
  (:use arcadia.core
        catcon.interop
        catcon.linear)
  (:import [UnityEngine Debug Mathf Time Vector3 Ray RaycastHit Physics Input]))

(defcomponent repulsor []
  (Update [this]
    (set! (.. this transform localScale)
          (v* (+ 10 (Mathf/Sin Time/time))
              Vector3/one))))

(defn update-repulsor [^MoveRepulsor this]
  (if (mouse?)
    (let [ray (.. this camera (ScreenPointToRay Input/mousePosition))]
      (if-let [^RaycastHit hit (raycast ray)]
        (do
          (set! (.. (object-named "Repulsor") transform position)
                (.. hit point))
          (set! (.. this (GetComponent catcon.camera.Camera) target)
                (.. hit point)))))))

(defcomponent MoveRepulsor []
  (Update [this]
      (update-repulsor this)))