//const vivaldi = require('vivaldi-coordinates');
//var index_update = require('./index');
//var local_pos = vivaldi.create();	// create new empty pos;
//console.log(`${local_pos.getCoordinates().x}`);
//var remote_pos;	// position from some remote host
//console.log(remote_pos);
//var rtt = 7;	  // ping time to remote host

//vivaldi.update(rtt, local_pos, remote_pos);	// update local position
//console.log(local_pos);
//console.log(remote_pos);
//console.log(`${local_pos.getCoordinates().x}`);

const vivaldi = require('vivaldi-coordinates');
var local_pos = vivaldi.create();	// create new empty pos;
console.log(`${local_pos.getCoordinates().x}`);
var remote_pos;	// position from some remote host
var rtt = 7;	  // ping time to remote host

vivaldi.update(rtt, local_pos, remote_pos);	// update local position
console.log(`${local_pos.getCoordinates().x}`);