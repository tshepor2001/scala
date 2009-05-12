/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: Map.scala 16884 2009-01-09 16:52:09Z cunei $


package scala.collection.generic

/** A generic template for mutable maps from keys of type A to values of type B.
 *  To implement a concrete mutable map, you need to provide implementations of the following methods:
 *
 *   def get(key: A): Option[B]
 *   def elements: Iterator[(A, B)]
 *   def put(key: A, value: B): Option[B]
 *   def remove(key: A): Option[B]
 *
 * If you wish that methods like, take, drop, filter return the same kind of map, you should also
 * override:
 *
 *   def empty: This
 *
 * If you to avoid the unncessary construction of an Option object,
 * you could also override apply, update, and delete.
 * It is also good idea to override methods `foreach` and `size` for efficiency.
 *
 */
trait MutableMapTemplate[A, B, +This <: MutableMapTemplate[A, B, This] with mutable.Map[A, B]]
  extends MapTemplate[A, B, This]
     with Builder[(A, B), This]
     with Growable[(A, B)]
     with Shrinkable[A]
     with Cloneable[This]
{ self =>

  override protected[this] def newBuilder: Builder[(A, B), This] = new MutableMapBuilder[A, B, This](empty.asInstanceOf[This]) // !!! concrete overrides abstract problem

  /** Adds a new mapping from <code>key</code>
   *  to <code>value</code> to the map. If the map already contains a
   *  mapping for <code>key</code>, it will be overridden.
   *
   * @param key    The key to update
   * @param value  The new value
   */
  def put(key: A, value: B): Option[B]

  /** Adds a new mapping from <code>key</code>
   *  to <code>value</code> to the map. If the map already contains a
   *  mapping for <code>key</code>, it will be overridden.
   *  @param key    The key to update
   *  @param value  The new value
   *  @return   An option consisting of value associated previously associated with `key` in the map,
   *            or None if `key` was not yet defined in the map.
   */
  def update(key: A, elem: B) { put(key, elem) }

  /** Add a new key/value mapping this map.
   *  @param    kv the key/value pair.
   *  @return   the map itself
   */
  def += (kv: (A, B)): this.type = { update(kv._1, kv._2); this }

  /** Create a new map consisting of all elements of the current map
   *  plus the given mapping from `key` to `value`.
   *  @param key    The key to add
   *  @param value  The new value
   *  @return       A fresh immutable map
   */
  def updated[B1 >: B](key: A, value: B1): collection.Map[A, B1] =
    (Map[A, B1]() plusAll thisCollection).updated[B1](key, value)

  /** Create a  new map consisting of all elements of the current map
   *  except any mapping from `key`.
   *  @param    key the key to be removed
   *  @return   A new map without a binding for <code>key</code>
   */
  def minus (key: A): This = clone() minus key

  /** Add a new key/value mapping and return the map itself.
   *
   *  @param kv    the key/value mapping to be added
   *  @deprecated  This operation will create a new map in the future. To add
   *               an element as a side effect to an existing map and return
   *               that map itself, use +=. If you do want to create a fresh map,
   *               you can use `plus` to avoid a @deprecated warning.
   */
  @deprecated def +(kv: (A, B)): this.type = { update(kv._1, kv._2); this }

  /** Adds two or more key/value mappings and return the map itself.
   *  with the added elements.
   *
   *  @param elem1 the first element to add.
   *  @param elem2 the second element to add.
   *  @param elems the remaining elements to add.
   *  @deprecated  This operation will create a new map in the future. To add
   *               an element as a side effect to an existing map and return
   *               that map itself, use +=. If you do want to create a fresh map,
   *               you can use `plus` to avoid a @deprecated warning.
   */
  @deprecated def +(elem1: (A, B), elem2: (A, B), elems: (A, B)*): this.type =
    this += elem1 += elem2 ++= elems

  /** Adds a number of elements provided by a traversable object
   *  via its <code>elements</code> method and returns
   *  either the collection itself (if it is mutable), or a new collection
   *  with the added elements.
   *  @deprecated  This operation will create a new map in the future. To add
   *               elements as a side effect to an existing map and return
   *               that map itself, use ++=. If you do want to create a fresh map,
   *               you can use `plusAll` to avoid a @deprecated warning.
   *  @param iter     the traversable object.
   */
  @deprecated def ++(iter: Traversable[(A, B)]): this.type = { for (elem <- iter) +=(elem); this }

  /** Adds a number of elements provided by an iterator
   *  via its <code>elements</code> method and returns
   *  the collection itself.
   *  @deprecated  This operation will create a new map in the future. To add
   *               elements as a side effect to an existing map and return
   *               that map itself, use ++=. If you do want to create a fresh map,
   *               you can use `plus` to avoid a @deprecated warning.
   *
   *  @param iter   the iterator
   */
  @deprecated def ++(iter: Iterator[(A, B)]): this.type = { for (elem <- iter) +=(elem); this }

  /** If given key is defined in this map, remove it and return associated value as an Option.
   *  If key is not present return None.
   *  @param    key the key to be removed
   */
  def remove(key: A): Option[B]

  /** Delete a key from this map if it is present.
   *  @param    key the key to be removed
   */
  def delete (key: A) { remove(key) }

  /** Delete a key from this map if it is present.
   *  @param    key the key to be removed
   *  @note     same as `delete`.
   */
  def -= (key: A): this.type = { delete(key); this }

  /** Delete a key from this map if it is present and return the map itself.
   *  @param    key the key to be removed
   *  @deprecated  This operation will create a new map in the future. To add
   *               elements as a side effect to an existing map and return
   *               that map itself, use -=. If you do want to create a fresh map,
   *               you can use `minus` to avoid a @deprecated warning.
   */
  @deprecated override def -(key: A): This = { -=(key); thisCollection }

  /** If given key is defined in this map, remove it and return associated value as an Option.
   *  If key is not present return None.
   *  @param    key the key to be removed
   *  @deprecated  Use `remove` instead.
   */
   @deprecated def removeKey(key: A): Option[B] = remove(key)


  /** Removes all elements from the set. After this operation is completed,
   *  the set will be empty.
   */
  def clear() { for ((k, v) <- elements) -=(k) }

  /** Check if this map maps <code>key</code> to a value.
    * Return that value if it exists, otherwise put <code>default</code>
    * as that key's value and return it.
    */
  def getOrElseUpdate(key: A, default: => B): B =
    get(key) match {
      case Some(v) => v
      case None => val d = default; this(key) = d; d
    }

  /** This function transforms all the values of mappings contained
   *  in this map with function <code>f</code>.
   *
   * @param f  The transformation to apply
   */
  def transform(f: (A, B) => B): this.type = {
    elements foreach {
      case (key, value) => update(key, f(key, value))
    }
    this
  }

  /** Retain only those mappings for which the predicate
   *  <code>p</code> returns <code>true</code>.
   *
   * @param p  The test predicate
   * @deprecated cannot be type inferred because of retain in Iterable.
   */
  def retain(p: (A, B) => Boolean): this.type = {
    for ((k, v) <- this) if (!p(k, v)) -=(k)
    this
  }

  override def clone(): This =
    empty ++ thisCollection

  /** The result when this map is used as a builder */
  def result: This = thisCollection

  /** Removes two or more elements from this collection and returns
   *  the collection itself.
   *
   *  @param elem1 the first element to remove.
   *  @param elem2 the second element to remove.
   *  @param elems the remaining elements to remove.
   *  @deprecated  use -= instead if you inted to remove by side effect from an existing collection.
   *               Use `minus` if you intend to create a new collection.
   */
  @deprecated override def -(elem1: A, elem2: A, elems: A*): This = {
    this -= elem1 -= elem2 --= elems
    thisCollection
  }

  /** Removes a number of elements provided by a traversible object and returns
   *  the collection itself.
   *  @deprecated  use --= instead if you inted to remove by side effect from an existing collection.
   *               Use `minusAll` if you intend to create a new collection.
   *
   *  @param iter     the iterable object.
   */
  @deprecated override def --(iter: Traversable[A]): This = {
    for (elem <- iter) -=(elem)
    thisCollection
  }


  /** Removes a number of elements provided by an iterator and returns
   *  the collection itself.
   *
   *  @param iter   the iterator
   *  @deprecated  use --= instead if you inted to remove by side effect from an existing collection.
   *               Use `minusAll` if you intend to create a new collection.
   */
  @deprecated override def --(iter: Iterator[A]): This = {
    for (elem <- iter) -=(elem)
    thisCollection
  }
}
