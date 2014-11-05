using UnityEngine;
using System.Collections;

public class PhysicsShim : MonoBehaviour {
  public static object Raycast(Ray ray) {
    RaycastHit hitInfo;
    if(Physics.Raycast(ray, out hitInfo)) {
      return hitInfo;
    } else {
      return null;
    }
  }
}
