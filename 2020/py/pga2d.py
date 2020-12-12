"""2D Projective Geometric Algebra."""

import math


class PGA2D:

  def __init__(self, value=0, index=0):
    """Initiate a new PGA2D.
         
        Optional, the component index can be set with value.
        """
    self.mvec = [0] * 8
    self._base = ["1", "e0", "e1", "e2", "e01", "e20", "e12", "e012"]
    if (value != 0):
      self.mvec[index] = value

  @classmethod
  def fromarray(cls, array):
    """Initiate a new PGA2D from an array-like object.

        The first axis of the array is assumed to correspond to the elements
        of the algebra, and needs to have the same length. Any other dimensions
        are left unchanged, and should have simple operations such as addition 
        and multiplication defined. NumPy arrays are therefore a perfect 
        candidate. 

        :param array: array-like object whose length is the dimension of the algebra.
        :return: new instance of PGA2D.
        """
    self = cls()
    if len(array) != len(self):
      raise TypeError('length of array must be identical to the dimension '
                      'of the algebra.')
    self.mvec = array
    return self

  def __str__(self):
    if isinstance(self.mvec, list):
      res = ' + '.join(
          filter(None,
                 [("%.7f" % x).rstrip("0").rstrip(".") +
                  (["", self._base[i]][i > 0]) if abs(x) > 0.000001 else None
                  for i, x in enumerate(self)]))
    else:  # Assume array-like, redirect str conversion
      res = str(self.mvec)
    if (res == ''):
      return "0"
    return res

  def __getitem__(self, key):
    return self.mvec[key]

  def __setitem__(self, key, value):
    self.mvec[key] = value

  def __len__(self):
    return len(self.mvec)

  def __invert__(a):
    """PGA2D.Reverse
        
        Reverse the order of the basis blades.
        """
    res = a.mvec.copy()
    res[0] = a[0]
    res[1] = a[1]
    res[2] = a[2]
    res[3] = a[3]
    res[4] = -a[4]
    res[5] = -a[5]
    res[6] = -a[6]
    res[7] = -a[7]
    return PGA2D.fromarray(res)

  def Dual(a):
    """PGA2D.Dual
        
        Poincare duality operator.
        """
    res = a.mvec.copy()
    res[0] = a[7]
    res[1] = a[6]
    res[2] = a[5]
    res[3] = a[4]
    res[4] = a[3]
    res[5] = a[2]
    res[6] = a[1]
    res[7] = a[0]
    return PGA2D.fromarray(res)

  def Conjugate(a):
    """PGA2D.Conjugate
        
        Clifford Conjugation
        """
    res = a.mvec.copy()
    res[0] = a[0]
    res[1] = -a[1]
    res[2] = -a[2]
    res[3] = -a[3]
    res[4] = -a[4]
    res[5] = -a[5]
    res[6] = -a[6]
    res[7] = a[7]
    return PGA2D.fromarray(res)

  def Involute(a):
    """PGA2D.Involute
        
        Main involution
        """
    res = a.mvec.copy()
    res[0] = a[0]
    res[1] = -a[1]
    res[2] = -a[2]
    res[3] = -a[3]
    res[4] = a[4]
    res[5] = a[5]
    res[6] = a[6]
    res[7] = -a[7]
    return PGA2D.fromarray(res)

  def __mul__(a, b):
    """PGA2D.Mul
        
        The geometric product.
        """
    if type(b) in (int, float):
      return a.muls(b)
    res = a.mvec.copy()
    res[0] = b[0] * a[0] + b[2] * a[2] + b[3] * a[3] - b[6] * a[6]
    res[1] = b[1] * a[0] + b[0] * a[1] - b[4] * a[2] + b[5] * a[3] + b[2] * a[
        4] - b[3] * a[5] - b[7] * a[6] - b[6] * a[7]
    res[2] = b[2] * a[0] + b[0] * a[2] - b[6] * a[3] + b[3] * a[6]
    res[3] = b[3] * a[0] + b[6] * a[2] + b[0] * a[3] - b[2] * a[6]
    res[4] = b[4] * a[0] + b[2] * a[1] - b[1] * a[2] + b[7] * a[3] + b[0] * a[
        4] + b[6] * a[5] - b[5] * a[6] + b[3] * a[7]
    res[5] = b[5] * a[0] - b[3] * a[1] + b[7] * a[2] + b[1] * a[3] - b[6] * a[
        4] + b[0] * a[5] + b[4] * a[6] + b[2] * a[7]
    res[6] = b[6] * a[0] + b[3] * a[2] - b[2] * a[3] + b[0] * a[6]
    res[7] = b[7] * a[0] + b[6] * a[1] + b[5] * a[2] + b[4] * a[3] + b[3] * a[
        4] + b[2] * a[5] + b[1] * a[6] + b[0] * a[7]
    return PGA2D.fromarray(res)

  __rmul__ = __mul__

  def __xor__(a, b):
    res = a.mvec.copy()
    res[0] = b[0] * a[0]
    res[1] = b[1] * a[0] + b[0] * a[1]
    res[2] = b[2] * a[0] + b[0] * a[2]
    res[3] = b[3] * a[0] + b[0] * a[3]
    res[4] = b[4] * a[0] + b[2] * a[1] - b[1] * a[2] + b[0] * a[4]
    res[5] = b[5] * a[0] - b[3] * a[1] + b[1] * a[3] + b[0] * a[5]
    res[6] = b[6] * a[0] + b[3] * a[2] - b[2] * a[3] + b[0] * a[6]
    res[7] = b[7] * a[0] + b[6] * a[1] + b[5] * a[2] + b[4] * a[3] + b[3] * a[
        4] + b[2] * a[5] + b[1] * a[6] + b[0] * a[7]
    return PGA2D.fromarray(res)

  def __and__(a, b):
    res = a.mvec.copy()
    res[7] = 1 * (a[7] * b[7])
    res[6] = 1 * (a[6] * b[7] + a[7] * b[6])
    res[5] = 1 * (a[5] * b[7] + a[7] * b[5])
    res[4] = 1 * (a[4] * b[7] + a[7] * b[4])
    res[3] = 1 * (a[3] * b[7] + a[5] * b[6] - a[6] * b[5] + a[7] * b[3])
    res[2] = 1 * (a[2] * b[7] - a[4] * b[6] + a[6] * b[4] + a[7] * b[2])
    res[1] = 1 * (a[1] * b[7] + a[4] * b[5] - a[5] * b[4] + a[7] * b[1])
    res[0] = 1 * (
        a[0] * b[7] + a[1] * b[6] + a[2] * b[5] + a[3] * b[4] + a[4] * b[3] +
        a[5] * b[2] + a[6] * b[1] + a[7] * b[0])
    return PGA2D.fromarray(res)

  def __or__(a, b):
    res = a.mvec.copy()
    res[0] = b[0] * a[0] + b[2] * a[2] + b[3] * a[3] - b[6] * a[6]
    res[1] = b[1] * a[0] + b[0] * a[1] - b[4] * a[2] + b[5] * a[3] + b[2] * a[
        4] - b[3] * a[5] - b[7] * a[6] - b[6] * a[7]
    res[2] = b[2] * a[0] + b[0] * a[2] - b[6] * a[3] + b[3] * a[6]
    res[3] = b[3] * a[0] + b[6] * a[2] + b[0] * a[3] - b[2] * a[6]
    res[4] = b[4] * a[0] + b[7] * a[3] + b[0] * a[4] + b[3] * a[7]
    res[5] = b[5] * a[0] + b[7] * a[2] + b[0] * a[5] + b[2] * a[7]
    res[6] = b[6] * a[0] + b[0] * a[6]
    res[7] = b[7] * a[0] + b[0] * a[7]
    return PGA2D.fromarray(res)

  def __add__(a, b):
    """PGA2D.Add
        
        Multivector addition
        """
    if type(b) in (int, float):
      return a.adds(b)
    res = a.mvec.copy()
    res[0] = a[0] + b[0]
    res[1] = a[1] + b[1]
    res[2] = a[2] + b[2]
    res[3] = a[3] + b[3]
    res[4] = a[4] + b[4]
    res[5] = a[5] + b[5]
    res[6] = a[6] + b[6]
    res[7] = a[7] + b[7]
    return PGA2D.fromarray(res)

  __radd__ = __add__

  def __sub__(a, b):
    """PGA2D.Sub
        
        Multivector subtraction
        """
    if type(b) in (int, float):
      return a.subs(b)
    res = a.mvec.copy()
    res[0] = a[0] - b[0]
    res[1] = a[1] - b[1]
    res[2] = a[2] - b[2]
    res[3] = a[3] - b[3]
    res[4] = a[4] - b[4]
    res[5] = a[5] - b[5]
    res[6] = a[6] - b[6]
    res[7] = a[7] - b[7]
    return PGA2D.fromarray(res)

  def __rsub__(a, b):
    """PGA2D.Sub
                
        Multivector subtraction
        """
    return b + -1 * a

  def smul(a, b):
    res = a.mvec.copy()
    res[0] = a * b[0]
    res[1] = a * b[1]
    res[2] = a * b[2]
    res[3] = a * b[3]
    res[4] = a * b[4]
    res[5] = a * b[5]
    res[6] = a * b[6]
    res[7] = a * b[7]
    return PGA2D.fromarray(res)

  def muls(a, b):
    res = a.mvec.copy()
    res[0] = a[0] * b
    res[1] = a[1] * b
    res[2] = a[2] * b
    res[3] = a[3] * b
    res[4] = a[4] * b
    res[5] = a[5] * b
    res[6] = a[6] * b
    res[7] = a[7] * b
    return PGA2D.fromarray(res)

  def sadd(a, b):
    res = a.mvec.copy()
    res[0] = a + b[0]
    res[1] = b[1]
    res[2] = b[2]
    res[3] = b[3]
    res[4] = b[4]
    res[5] = b[5]
    res[6] = b[6]
    res[7] = b[7]
    return PGA2D.fromarray(res)

  def adds(a, b):
    res = a.mvec.copy()
    res[0] = a[0] + b
    res[1] = a[1]
    res[2] = a[2]
    res[3] = a[3]
    res[4] = a[4]
    res[5] = a[5]
    res[6] = a[6]
    res[7] = a[7]
    return PGA2D.fromarray(res)

  def ssub(a, b):
    res = a.mvec.copy()
    res[0] = a - b[0]
    res[1] = -b[1]
    res[2] = -b[2]
    res[3] = -b[3]
    res[4] = -b[4]
    res[5] = -b[5]
    res[6] = -b[6]
    res[7] = -b[7]
    return PGA2D.fromarray(res)

  def subs(a, b):
    res = a.mvec.copy()
    res[0] = a[0] - b
    res[1] = a[1]
    res[2] = a[2]
    res[3] = a[3]
    res[4] = a[4]
    res[5] = a[5]
    res[6] = a[6]
    res[7] = a[7]
    return PGA2D.fromarray(res)

  def norm(a):
    return abs((a * a.Conjugate())[0])**0.5

  def inorm(a):
    return a.Dual().norm()

  def normalized(a):
    return a * (1 / a.norm())


e0 = PGA2D(1.0, 1)
e1 = PGA2D(1.0, 2)
e2 = PGA2D(1.0, 3)
e01 = PGA2D(1.0, 4)
e20 = PGA2D(1.0, 5)
e12 = PGA2D(1.0, 6)
e012 = PGA2D(1.0, 7)


def point(x, y):
  return x * e20 + y * e01 + e12


def direction(x, y):
  return x * e20 + y * e01


def line(a, b, c):
  return a * e1 + b * e2 + c * e0


if __name__ == '__main__':
  print("e0*e0         :", str(e0 * e0))
  print("pss           :", str(e012))
  print("pss*pss       :", str(e012 * e012))
