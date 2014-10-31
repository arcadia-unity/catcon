(ns catcon.linear
  (:import [UnityEngine Quaternion Vector3])
  (:require [catcon.interop :as iop]))

;; ============================================================
;; vectors
;; ============================================================

(defn v3 ^Vector3 [x y z]
  (Vector3. x y z))

(defn v- ^Vector3 [^Vector3 v1 ^Vector3 v2]
  (Vector3/op_Subtraction v1 v2))

(defn v+ ^Vector3 [^Vector3 v1 ^Vector3 v2]
  (Vector3/op_Addition v1 v2))

(defn vmult-scalar ^Vector3 [^Vector3 v s]
  (Vector3/op_Multiply v (float s)))

;; name might be improved
(defn vmult-vec ^Vector3 [^Vector3 v1 ^Vector3 v2]
  (Vector3/Scale v1 v2))

(defn v* ^Vector3 [a b]
  (if (instance? Vector3 a)
    (if (instance? Vector3 b)
      (vmult-vec a b)
      (vmult-scalar a b))
    (if (instance? Vector3 b)
      (vmult-scalar b a)
      (throw
        (ArgumentException.
          "v* expects at least one Vector3")))))

;; ============================================================
;; quaternions
;; ============================================================

(defn q* [^Quaternion q, a]
  (iop/condcast-> a a
    Vector3    (Quaternion/op_Multiply q a)
    Quaternion (Quaternion/op_Multiply q a)))

(defn qe ^Quaternion [^Vector3 v]
  (Quaternion/Euler v))

(defn qt ^Quaternion [x y z w]
  (Quaternion. x y z w))

;; ============================================================
;; rotate
;; ============================================================

(defn point-pivot ^Vector3 [^Vector3 pt, ^Vector3 pv, ang]
  (let [angq (cond 
               (instance? Quaternion ang) ang
               (instance? Vector3 ang)    (qe ang)
               :else (throw
                       (ArgumentException.
                         (str "Expect rotation as Quaternion or Vector3, got "
                           (type ang)))))]
    (v+ (q* angq (v- pt pv)) pv)))
