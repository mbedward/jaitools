Language Summary
================

Functions
---------

General numeric functions
~~~~~~~~~~~~~~~~~~~~~~~~~

===============  ====================   =====================  =====================  ===========================
Name             Description            Arguments              Returns                Notes
===============  ====================   =====================  =====================  ===========================
``abs(x)``       Absolute value         double value           absolute value of x

``acos(x)``      Arc-cosine             value in range [-1,1]  angle in radians

``asin(x)``      Arc-sine               value in range [-1,1]  angle in radians

``atan(x)``      Arc-tangent            value in range [-1,1]  angle in radians

``cos(x)``       Cosine                 angle in radians       cosine [-1, 1]

``degToRad(x)``  Degrees to radians     angle in radians       angle in degrees

``exp(x)``       Exponential            double value           e to the power x       

``floor(x)``     Floor                  double value           integer part of x
                                                               as a double

``isinf(x)``     Is infinite            double value           1 if x is positive
                                                               or negative infinity;
                                                               0 otherwise

``isnan(x)``     Is NaN                 double value           1 if x is equal to     
                                                               Java's Double.NaN;
                                                               0 otherwise

``isnull(x)``    Is null                double value           1 if x is null;        Equivalent to isnan(x)
                                                               0 otherwise

``log(x)``       Natural logarithm      positive value         logarithm to base e

``log(x, b)``    General logarithm      x: positive value;     logarithm to base b
                                        b: base
                                    
``radToDeg(x)``  Radians to degrees     angle in radians       angle in degrees

``rand(x)``      Pseudo-random number   double value           value in range [0, x)  Volatile function

``randInt(x)``   Pseudo-random number   double value           integer part of value  Equivalent to ``floor(rand(x))``
                                                               in range [0, x)
                                                               
``round(x)``     Round                  double value           rounded value     

``round(x, n)``  Round to multiple of   x: double value;       value rounded to       E.g. ``round(44.5, 10)``
                 n                      n: whole number        nearest multiple of n  returns 40
                 
``sin(x)``       Sine                   angle in radians       sine [-1, 1]

``sqrt(x)``      Square-root            non-negative value     square-root of x

``tan(x)``       Tangent                angle in radians       double value
===============  ====================   =====================  =====================  ===========================


Logical functions
~~~~~~~~~~~~~~~~~

===================      ====================   =====================  =====================
Name                     Description            Arguments              Returns             
===================      ====================   =====================  =====================
``con(x)``               Conditional            double value           1 if x is non-zero;
                                                                       0 otherwise

``con(x, a)``            Conditional            double values          a if x is non-zero;
                                                                       0 otherwise

``con(x, a, b)``         Conditional            double values          a if x is non-zero;
                                                                       b otherwise

``con(x, a, b, c)``      Conditional            double values          a if x is positive;
                                                                       b if x is zero;
                                                                       c if x is negative

===================      ====================   =====================  =====================

Statistical functions
~~~~~~~~~~~~~~~~~~~~~

================  ====================   =====================  =========================
Name              Description            Arguments              Returns               
================  ====================   =====================  =========================
``max(x, y)``     Maximum                double values          maximum of x and y

``max(ar)``       Maximum                array                  maximum of array values 

``mean(ar)``      Mean                   array                  mean of array values

``min(x, y)``     Minimum                double values          minimum of x and y

``min(ar)``       Minimum                array                  minimum of array values

``median(ar)``    Median                 array                  median of array values

``mode(ar)``      Mode                   array                  mode of array values

``range(ar)``     Range                  array                  range of array values

``sdev(ar)``      Standard deviation     array                  sample standard deviation
                                                                of array values

``sum(ar)``       Sum                    array                  sum of array values

``variance(ar)``  Variance               array                  sample variance of array
                                                                values

================  ====================   =====================  =========================

Processing area functions
~~~~~~~~~~~~~~~~~~~~~~~~~

===============   ================================================
Name              Returns             
===============   ================================================
``height()``      Height of the processing area (pixels)

``width()``       Width of the processing area (pixels)

``size()``        Total size of the processing area (pixels)

``xmin()``        Minimum X ordinate of the processing area

``ymin()``        Minimum Y ordinate of the processing area

``xmax()``        Maximum X ordinate of the processing area

``ymax()``        Maximum Y ordinate of the processing area

``x()``           X ordinate of the current destination pixel

``y()``           Y ordinate of the current destination pixel

===============   ================================================

