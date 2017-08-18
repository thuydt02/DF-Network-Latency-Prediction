/*
The file is to compute a competed matrix from a matrix with missing values 
input: a matrix with missing values in 2 files
output: a completed matrix in a file
The algorithm follows vivaldi algorithm => It is Euclidean Embbedding
*/

const fs = require('fs');
const vivaldi = require('vivaldi-coordinates');

const filename = process.argv[2] || '/home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/PlanetLabData_1';
const iterations = Number(process.argv[3]) || 1;
const missed_fname = process.argv[4] || '/home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/W80N490' ;// 20% missing values
//const F_hat_name = process.argv[5] || '/home/phuongdm/Documents/THUYDT/NetBeansProjects/Liu_Network_Latency/Data/D_hat.txt';


const data = fs.readFileSync(filename, 'utf-8')
	.split('\n')
	.map(line => line.split(','))
	.map(arr => arr.map(Number));

const knownMat = fs.readFileSync(missed_fname, 'utf-8')
	.split('\n')
	.map(line => line.split(','))
	.map(arr => arr.map(Number));

const N = data.length;
const maxData = data.reduce((val, line) => Math.max(val, ...line), 0);
//console.log(`Vivaldi ${N}`);
var F = []; // the latency matrix with missing values
var F_hat = []; //the completed latency matrix will be computed
for (let i = 0; i<N ;i++ )
{
	F[i] = [];
	for (let j=0; j<N;j++ )
	{
		F[i][j] = data[i][j]*knownMat[i][j]/maxData;
	}
}


const max_xy = 10;
const random = () => new vivaldi.HeightCoordinates(Math.random() * max_xy, Math.random() * max_xy, Math.random());
//const random = () => new vivaldi.HeightCoordinates(Math.random() * max_xy, Math.random() * max_xy,0);
const A = data.map(() => vivaldi.create(random()));



// compute the coordinates of N points corresponding N nodes
for (let x = 0; x < iterations; x++) {
	for (let i = 0; i < N; ++i) for (let j = 0; j < N; ++j) if (i != j)
 	{
		vivaldi.update(F[i][j], A[i], A[j]);
	}
}

//compute the completed matrix F_hat

	for (let i = 0; i < N; ++i) {
		F_hat[i] = [];
		for (let j = 0; j < N; ++j) F_hat[i][j] = vivaldi.distance(A[i], A[j]);
	}

//-----------------------------------------------------------------------------------------------------
// Export F_hat, normalized data to files for SVR
//-----------------------------------------------------------------------------------------------------
console.log(N);
//console.log(`${F_hat[489][489]}`);
//process.stdout.write(`${F_hat[100][100]}`);
for (let i =0; i<N; i++) for(let j=0; j<N; j++) 
	if (j != N-1) {process.stdout.write(`${F_hat[i][j]}` + ' ');}
	else {if (i < N-1) process.stdout.write(`${F_hat[i][j]}` + '\n'); else process.stdout.write(`${F_hat[i][j]}`);}



//console.log(`${A[i].getCoordinates().x} ${A[i].getCoordinates().y} ${A[i].getCoordinates().h}`);

//const F_hat_name = "C:/ThuyDT/NetBeansProjects/Liu_NetworkLatency/data/D_hat.txt";

//var F_hat_ = require("fs");
/*var F_hat_2 = require("fs");

F_hat_.stat(F_hat_name, function(err, stat) {
    if(err == null) {
        F_hat_.unlink(F_hat_name);
    }
});
*/
/*
var stream_F_hat_ = F_hat_.createWriteStream(F_hat_name, {flags:'w'});
var st = "";
//console.log(N);
for (let i=0;i<N ;++i ) {
	st = F_hat[i][0];	
	for (let j=1;j<N ;++j ) st = st + "," + F_hat[i][j] ;	
	if (i != N-1)	
		stream_F_hat_.write(st+'\n');		
	else 		stream_F_hat_.write(st);		
}
stream_F_hat_.end();
*/
