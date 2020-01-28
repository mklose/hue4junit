[![ci-badge]][ci-actions]
[![Maven Central](https://img.shields.io/maven-central/v/de.klosebrothers.hue/hue4junit.svg?label=maven)](https://search.maven.org/search?q=g:%22de.klosebrothers.hue%22%20AND%20a:%22hue4junit%22)

This library provides a JUnit5 TestExecutionListener for [Philips hue lights ](https://www2.meethue.com/en-us) to indicate JUnit test run results by turning light bulbs green or red.

# how to
## setup hue
open [setup_hue.html](http://htmlpreview.github.io/?https://github.com/mklose/hue4junit/blob/master/setup_hue.html) and use returned value of __username__ as `hue.username` (you will need to press the button on your hue bridge).

`hue.username` will be retrieved in this order from :
 1. [hue4junit.properties](hue4junit.properties) in project folder
 1. resource file [hue4junit.properties](src/test/resources/hue4junit.properties)
 1. System Properties
 
 ### optional settings are:
 these settings will be retrieved the same way as `hue.username`. 
  - `hue.ip` , unless set we try to get this value via [meethue](https://www.meethue.com/api/nupnp)
  - `hue.timeout` , timeout for calls to hue bridge, default `5000`
  - `hue.listener.lamps` , ids of lamps to be used, default is  `[1, 2, 3]`

## add to your project

see [here](https://github.com/swkBerlin/kata-bootstraps/tree/master/java/hue4j5) howto add hue4junit to your project

[ci-badge]: https://github.com/mklose/hue4junit/workflows/CI/badge.svg "CI build status"
[ci-actions]: https://github.com/mklose/hue4junit/actions