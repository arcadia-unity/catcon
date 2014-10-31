(ns catcon.cat
  (:use arcadia.core
        catcon.linear)
  (:import [UnityEngine Time Vector3 Mathf Physics Gizmos Color Transform]))

(defn random-vector ^Vector3 []
  (v* (UnityEngine.Random/onUnitSphere) 
      (Vector3. 1 0 1)))

(declare cat-update)

(defcomponent Cat [^float rotate-speed
                   heading]
  (Start [this]
    (set! rotate-speed 1.0)
    (set! (.. this heading) (agent (random-vector))))
  
  (Update [this]
    (catcon.cat/cat-update this))
  (OnDrawGizmos [this]
    (set! Gizmos/color Color/green)
    (if heading
      (Gizmos/DrawRay (.. this transform position)
                      @(.. this heading)))
    (set! Gizmos/color Color/red)
    (Gizmos/DrawRay (.. this transform position)
                    (.. this transform forward))))

(defn flockmates [^Vector3 pos ^double radius]
  (->> (Physics/OverlapSphere pos radius Physics/AllLayers)
       (filter #(= (.tag %) "Cat"))))

(defn near [^Vector3 pos ^double radius ^System.Type component]
  (->> (flockmates pos radius)
       (map #(get-component % component))))

(defn near-headings [^Vector3 pos ^double radius]
  (->> (near pos radius catcon.cat.Cat)
       (map #(deref (.heading %)))))

(defn v÷ ^Vector3 [^Vector3 v1 ^Vector3 v2]
  (Vector3/op_Division v1 v2))

(defn alignment [h headings speed]
  (let [average (v÷ (reduce v+ headings) (count headings))]
    (Vector3/RotateTowards h average speed 0)))

(defn cohesion [h p positions speed]
  (let [average (v÷ (reduce v+ positions) (count positions))]
    (Vector3/RotateTowards h (v- average p) speed 0)))

(defn separation [h p positions speed]
  (let [fs (map #(v- p %) positions)
        f (reduce v+ fs)]
    (Vector3/RotateTowards h f speed 0)))

(defn flock [heading position flock-positions flock-headings]
  (-> heading
      (cohesion position flock-positions 2)
      (separation position flock-positions 60)
      (alignment flock-headings 2)))

(defn cat-update [^Cat this]
  (let [position (.. this transform position)
        mates  (flockmates position 4)
        flock-positions (mapv #(.. % transform position) mates)
        flock-headings (mapv #(deref (.. % (GetComponent Cat) heading)) mates)
        
        speed (* Time/deltaTime (.. this rotate-speed))
        heading (send (.. this heading) flock
                      position
                      flock-positions
                      flock-headings)
        ^Vector3 heading @heading]
    (set! (.. this transform forward)
          (Vector3/RotateTowards (.. this transform forward)
                                 heading speed 0))))
; (defn cat-wander [^Cat this]
;   (set! (.. this heading) (random-vector)))

(comment
  (doseq [x (range 0 30 2)
          y (range 0 30 2)]
  (instantiate (object-named "/Cat") (Vector3. x 0 y)))

(dorun (map destroy (objects-named "Cat(Clone)")))

(set! (.angle (get-component (object-named "/Cat") catcon.cat.Cat)) 360)

(.GetInstanceID (object-named "/Cat"))

(set! (.. (object-named "/Cat") transform forward) Vector3/forward))

