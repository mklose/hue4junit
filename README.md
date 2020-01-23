[![ci-badge]][ci-actions]




This library provides a JUnit5 TestExecutionListener for [Philips hue lights ](https://www2.meethue.com/en-us) to indicate JUnit test run results by turning light bulbs green or red.

# how to
## setup hue
open [setup_hue.html](http://htmlpreview.github.io/?https://github.com/mklose/hue4junit/blob/master/setup_hue.html) and use returned value of __username__ as ``hue.client``.

``hue.client`` will be retrieved in this order from :
 1. resource file [__hue4junit.properties__](resources/hue4junit.properties)
 1. __hue4java.properties__ in project folder
 1. System Property ```hue.client```
 
 ### optional settings are:
 these settings will be retrieved the same way as ``hue.client``. 
  - ``hue.ip`` , unless set we try to get this value via [meethue](https://www.meethue.com/api/nupnp)
  - ``hue.timeout`` , timeout for calls to hue bridge, default ``5000``
  - ``hue.listener.lamps`` , ids of lamps to be used, dealut is  ``["1", "2", "3"]``

## add to your project

see [here](https://github.com/swkBerlin/kata-bootstraps/tree/master/java/hue4j5) howto add hue4junit to your project

[ci-badge]: https://github.com/mklose/hue4junit/workflows/CI/badge.svg "CI build status"
[ci-actions]: https://github.com/mklose/hue4junit/actions