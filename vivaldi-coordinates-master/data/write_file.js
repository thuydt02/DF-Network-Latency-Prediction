var fs = require('fs');

//create a file named mynewfile1.txt:



var stream = fs.createWriteStream("./output/append.txt", {flags:'a'});
for (let i = 1;i<1000 ;i++ )
  stream.write(i + "\n");

//console.log(new Date().toISOString());
stream.end();


//for (let i=1;i<=100 ;i++ )

//	fs.appendFileSync('./output/mynewfile1.txt', 'abc ' + i + ' \n', function (err) {
//	  if (err) throw err;
//	  console.log('Updated!');
//	});
//var fs = require('fs');

