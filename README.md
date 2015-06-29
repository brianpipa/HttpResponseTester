# HttpResponseTester
HTTPResponseTester is a standalone .war file that can be used for testing client code that talks to a remote (internal or external) webserver. With it, you can easily configure it to respond with the content, content type, status code, and an optional delay of your choosing. All URLs accept both GET and POST. 

Example uses:
* our code uses a Google webservice and I want to write robust handling of error cases. I change my code to connect to HTTPResponseTester instead of Google's server and use a HTTPResponseTester URL I have configured that will return a 503 error and I make sure my client code handles it properly
* our code talks to an internal webservice for data and I want to make sure the code properly times out after 5 minutes. I change our code to HTTPResponsetester instead of the webservice and I set up HTTPResponsetester so that it delays sending back a response for 6 minutes and I verify that the code properly timed out

The latest version of HTTPResponseTester can always be retrieved from http://github.com/brianpipa/HTTPResponseTester

To build, just run ant in the root - this will produce dist/responsetester.war

To use it, drop the responsetester.war into any servlet engine (like Tomcat) and access it via http://SERVER:PORT/responsetester

To configure it, edit the responsetester/response.properties file and add an entry or entries for what you're trying to test. There are comments/docs inside of it and a bunch of samples in there already so you should be able to figure out how to set it up. You can edit this properties file on the fly and it picks up the changes immediately. No need to restart the server. Put any files you want to serve into the responses folder.

To access it, use http://SERVER:PORT/responsetester?responsename=NAME where name is the name you used in the properties file. Valid sample urls are:
* help - this help file: http://SERVER:PORT/responsetester?responsename=help
* 404 - a fake 404 error: http://SERVER:PORT/responsetester?responsename=404
* 503 - a fake 503 error: http://SERVER:PORT/responsetester?responsename=503
* delayed - waits 10 seconds before responding: http://SERVER:PORT/responsetester?responsename=delayed
* html - an HTML response: http://SERVER:PORT/responsetester?responsename=html
* json - a JSON response: http://SERVER:PORT/responsetester?responsename=json
* test - plain text from a file: http://SERVER:PORT/responsetester?responsename=test

**Ideas for enhancement:**
* add in the ability to deny GET OR POST (or both?)
* support other params and even HTTP headers (like *header.required=X-Auth-Token*)
