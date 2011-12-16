ClouDJ
======
The ClouDJ webapp is a fun little tool for creating and listening to music
playlists. The basic idea is that you pick some song to be the first in your
playlist, either by searching for it or by clicking "Surprise Me...", and then
a 15 song playlist is automatically generated, made up of songs that go well
together. You can then listen to the playlist.

My intention with this assignment is to demonstrate my aptitude and fluency in
both client and server side development and architecture, the entire
application domain. I also aim to demonstrate a solid, general understanding of
how some core web technologies work. Additionally, I want to showcase a bit of
innovation, namely in the Phnqlets framework (described below), which is a
widget-based approach to rich web client architecture.

The webapp consumes two music-related web services: Last.fm
(http://www.last.fm/api) and Rdio (http://developer.rdio.com/docs/read/REST).

Build Requirements:
-------------------

	Apache Maven v3.03 - http://maven.apache.org/
	Mozilla Firefox
	Apache Tomcat v.7.0 - http://tomcat.apache.org/ (optional)

Note: All of the commands and paths listed below are assumed to be relative to
the root this package when expanded.

Build/Deploy/Test:
------------------
The following command will build the ClouDJ webapp, install it (plus all
generated dependencies) in your maven repository, and lastly run all of unit
tests, integration tests and browser tests:

	mvn install
	
This will take several minutes, especially the first time since many Maven
dependencies will need to be downloaded. The unit tests are fairly quick to
run, but the integration tests will take a while because they entail many
requests being made to remote web services. FYI, one of the browser
integration tests involves playing a 30 second track preview.

If you don't have Firefox installed, then the web integration tests will fail.

There is also a possibility that the integration tests may fail due to some
issues with the Rdio or Last.fm services. If this happens, then you can build
and install ClouDJ by running:

	mvn install -DskipTests

To run the unit tests alone (without the integration tests):

	mvn test
	
For the integration and browser tests:

	mvn integration-test


Starting the Server
-------------------
Start the embedded Jetty web server:

	mvn -pl cloudj/webapp jetty:run
	
Then, open http://localhost:54321 in a web browser to try out the app.

Alternatively, you can deploy the webapp to Tomcat by copying the following
war file to $TOMCAT_HOME/webapps/:

	cloudj/webapp/target/cloudj.war


Architectural Overview
----------------------
ClouDJ is a web application that serves up a rich, persistent JavaScript-based
UI (i.e. no page refreshes). The client communicates with the ClouDJ REST api
over XHR. The client-side functionality is built using a widget-based
framework called Phnqlets (pronounce "Funklets") -- more on Phnqlets below.
Some external JavaScript libraries are used; jQuery is extensively used,
jQuery UI is also used, as is a plugin called Isotope. The markup is generally
semantic and HTML5 (i.e. use of tags such as nav, header, section, etc.).
There is also extensive use of CSS3 features such as style attribute
animations.

The REST api uses Spring MVC for mapping requests. The client-side
functionality plus the REST interface collectively make up the top-most coarse
layer in the architecture -- the "webapp" layer.

The next layer down is the "app" layer, which contains all of the actual logic
for the application. Access to the app layer is via the Cloudj facade. In
fact, the webapp layer's REST API is basically very thin veneer over the
Cloudj facade. The app layer provides a few TO's (transfer objects, beans). It
also assimilates/brokers information to/from some public web services.

Below the app layer is a collection of utilities, not really specific to any
particular domain. This layer is called "core".

There are two public web service api wrappers, one for the public Last.fm api
(http://www.last.fm/api), and another for the Rdio api
(http://developer.rdio.com/docs/read/REST). These are in a group called
"clients".

Additionally, there is a group of 3rd party source code libraries called
"external".

The final piece is the Phnqlets framework. It is a framework for creating
chunks of client-side functionality. Each phnqlet is composed of HTML, CSS and
JavaScript, or a subset thereof. A phnqlet has a client-side lifecycle
expressed in JavaScript (i.e. init, onInsert, onShow, etc.). A phnqlet can
incorporate all three client-side technologies (HTML, JS, CSS), as in some
dynamic embedded widget, or it could be a simple JavaScript API, or even a
CSS-based theme. Phnqlets also include dependency management; that is,
phnqlets can declare other phnqlets as dependencies. Phnqlets can also be
embedded within Phnqlets in which case the dependency is implicit. Basically,
phnqlets facilitates the creation of rich web UIs, promoting a clean
separation between units of client-side functionality into discreet
components.

The ClouDJ webapp is organized into a hierarchy of phnqlets, the root being
one called cloudj.Main; there are several more embedded within.


Testing Approach
----------------
There are three levels of testing in ClouDJ: Unit Tests, Integration Tests,
Automated Browser Tests.

- Unit Tests: the unit tests cover the lower layers in the architecture such
  as core utilities and phnqlets. The unit tests are implemented with JUnit.

- Integration Tests: the Cloudj app facade, which is a Java API, is tested
  with JUnit. The REST API is tested with a Spring-based mock servlet stack to
  simulate HTTP requests, run from JUnit test cases.

- Automated Browser Tests: the web UI is tested using Selenium to provide
  remote control of a Firefox browser. The selenium calls are made from within
  JUnit test cases. To support browser testing, the Maven Jetty app server
  plugin is used; this allows all testing to be triggered exclusively via
  Maven.


Assumptions/Known Issues (i.e. stuff I would have done with more time)
======================================================================
- I have only tested this on a Mac on Safari, Chrome and Firefox. So, no
  Windows at all. Unfortunately, I only have Macs at my house and, as I have
  discovered, so do all my neighbours.

- There are definitely some usability issues. For example, there is no
  "player" per se -- i.e. no central play/pause with any indication of
  playback progress. More time would have meant more usability refinement for
  sure.

- I have done absolutely no i18n, but as matter of course, I am a staunch
  advocate of upfront i18n/l10n when there is time.
