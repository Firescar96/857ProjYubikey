if (Meteor.isClient) {
  Template.hello.greeting = function () {
    return "Welcome to Backend.";
  };

  Template.hello.events({
    'click input': function () {
      // template data, if any, is available in 'this'
      if (typeof console !== 'undefined')
        console.log("You pressed the button");
    }
  });
  
  rests = new Meteor.Collection("RESTS")

	Meteor.subscribe(function() {
 		rests.find().observe({
   		added: function(item){
			window.location = "com.firescar96.nom.appUser";
    		}
 	 	});
	});
}

if (Meteor.isServer) {
	var cheerio = Meteor.npmRequire('cheerio');

	Users = new Meteor.Collection('users');
	Events = new Meteor.Collection('events');

	fs = Npm.require( 'fs' ) ;

	Router.map(function () {
		this.route("/", {
	    		where: "server",
	    		action: function(){
	      		console.log('################################################');
			      //console.log(this.request.method);
			      //console.log(this.request.headers);

			      //console.log('------------------------------');
			      //console.log(this.request.body);
			      //console.log('------------------------------');

			      this.response.statusCode = 200;
			      this.response.setHeader("Content-Type", "application/json");
			      this.response.setHeader("Access-Control-Allow-Origin", "*");
			      this.response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

		     		if (this.request.method == 'POST') {
					fs.appendFile('/home/duffield/Dormbell/Backend/log/reqlog.txt', (new Date()).getTime() + ' POST: ' + JSON.stringify(this.request.body) + '\n', function (err) {console.log('error logging post');});
					//console.log(this.request.body);
					HandleData(this.request.body);
					this.response.writeHead(200, {'Content-Type': 'text/plain'});
					this.response.end("");
		      		}

		      		if (this.request.method == 'GET') 
				{        
					$ = cheerio.load(Meteor.http.get("http://my.yubico.com/neo/"+this.request.query.otp).content);
					var info = $('#info').text();
					console.log(info);
					console.log("\n\n\n");
					console.log("destination address: "+this.request.query.pa);
					console.log("amnt: "+this.request.query.amnt);
  					this.response.writeHead(200, {'Content-Type': 'text/plain'});
                                        this.response.end(info);
	      			}
			}
	  	});
	});

}
