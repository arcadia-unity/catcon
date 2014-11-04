(ns catcon.floor
  (:use arcadia.core
        catcon.linear
        catcon.interop)
  (:import [UnityEngine Debug Collision Transform GameObject]))

(declare collide)

(defcomponent floor [^int difficulty ^bool spawned]
  (OnCollisionEnter [this collision]
    (collide this collision)))

(defn rand-range
  ([^double min ^double max] (UnityEngine.Random/Range min max))
  ([^double v] (rand-range (- v) v)))

(defn scale [^Transform t ^double x ^double y ^double z]
  (set! (.localScale t) (v* (.localScale t)
                            (Vector3. x y z))))

(defmacro chance [p & body]
  `(if (< (rand) ~p)
    ~@body))

(defn spawn-floor [^GameObject obj ^double dist]
  (let [dist (+ 5 dist)
        current-x (.. obj transform localScale x)
        current-z (.. obj transform localScale z)
        x-scale (rand-range 0.5 1.5)
        z-scale (rand-range 0.5 1.5)
        y-offset (- 0 dist (rand-range 1))
        x-offset (* 0.5
                    (chance 0.5 1 -1)
                    (+ (* x-scale current-x)
                       current-x))
        z-offset (* 0.5
                    (chance 0.5 1 -1)
                    (+ (* z-scale current-z)
                       current-z))
        offset (if (< 0.5 (rand))
                 (Vector3. x-offset y-offset 0)
                 (Vector3. 0 y-offset z-offset))
        clone (instantiate obj)]
    (doto (.. clone transform)
      (.Translate offset)
      (scale x-scale 1 z-scale))
    clone))

(defn inc-difficulty [^floor f]
  (set! (.difficulty f) (inc (.difficulty f))))

(defn collide [^floor this ^Collision collision]
  (if (and (= "Cat" (.. collision gameObject tag))
           (not (.spawned this)))
    (do
      (-> (spawn-floor (.gameObject this)
                       (* 5 (.difficulty this)))
          (get-component floor)
          inc-difficulty)
      (set! (.spawned this) true)
      (GameObject/Destroy this))))