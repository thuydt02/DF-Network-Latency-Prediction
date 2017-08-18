const fs = require('fs');
const vivaldi = require('vivaldi-coordinates');

const filename = process.argv[2] || './PlanetLab/PlanetLabData_14';
const iterations = Number(process.argv[3]) || 1000;
const missed_fname = process.argv[4] || './PlanetLab/W20N490' ;// 20% missing values

const data = fs.readFileSync(filename, 'utf-8')
	.split('\n')
	.map(line => line.split(','))
	.map(arr => arr.map(Number));

data.pop(); // Removes last line

const knownMat = fs.readFileSync(missed_fname, 'utf-8')
	.split('\n')
	.map(line => line.split(','))
	.map(arr => arr.map(Number));

const N = data.length;
const maxData = data.reduce((val, line) => Math.max(val, ...line), 0);

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
const maxDist = data.reduce((val, line) => Math.max(val, ...line), 0);
console.log(`maximum distance = ${maxDist}`);

const max_xy = 10;
const random = () => new vivaldi.HeightCoordinates(Math.random() * max_xy, Math.random() * max_xy, Math.random());
//const random = () => new vivaldi.HeightCoordinates(Math.random() * max_xy, Math.random() * max_xy,0);
const A = data.map(() => vivaldi.create(random()));

console.log('\nRandomly Create points: \n');
for (let i = 0; i < 5; ++i) {
	console.log(`${A[i].getCoordinates().x} ${A[i].getCoordinates().y} ${A[i].getCoordinates().h}`);	
}
console.log('\n...');

// compute the coordinates of N points corresponding N nodes
for (let x = 0; x < iterations; x++) {
	for (let i = 0; i < N; ++i) for (let j = 0; j < N; ++j) if (i != j) {
		vivaldi.update(F[i][j], A[i], A[j]);
	}
}

//compute the completed matrix F_hat

	for (let i = 0; i < N; ++i) {
		F_hat[i] = [];
		for (let j = 0; j < N; ++j) F_hat[i][j] = vivaldi.distance(A[i], A[j]);
	}

//compute Root Mean Square Error and Mean Absolute Error
var RMSE = 0; var MAE = 0; var num_known_values = 0; var RMSE_test = 0; var MAE_test = 0;
for (let i=0;i<N ;i++ )for (let j=0;j<N ;j++ )
	if (i != j) {if (knownMat[i][j] != 0){
			RMSE = RMSE + Math.pow(F_hat[i][j] - F[i][j], 2); MAE = MAE + Math.abs(F_hat[i][j] - F[i][j]);num_known_values = num_known_values+1;}
	else{ RMSE_test = RMSE_test + Math.pow(F_hat[i][j] - data[i][j]/maxData, 2); MAE_test = MAE_test + Math.abs(F_hat[i][j] - data[i][j]/maxData);}}
if (num_known_values != 0)
{RMSE = Math.sqrt(RMSE/num_known_values); MAE = MAE/num_known_values;}
if (num_known_values < N*N-N)
{RMSE_test = Math.sqrt(RMSE_test/(N*N-N-num_known_values)); MAE_test = MAE_test/(N*N-N-num_known_values);}


//console.log(`${iterations} iterations, max error = ${maxError(N)}`);

console.log('\nNEW POINTS after doing vivaldi, based on latency matrix\n');
for (let i = 0; i < 5; ++i) {
	console.log(`${A[i].getCoordinates().x} ${A[i].getCoordinates().y} ${A[i].getCoordinates().h}`);
}

console.log(N);
console.log(`Root Mean Square Error of TRAIN : ${RMSE}`);
console.log(`Mean Absolute Error of TRAIN: ${MAE}`);
console.log(`Root Mean Square Error of TEST : ${RMSE_test}`);
console.log(`Mean Absolute Error of TEST: ${MAE_test}`);

//-----------------------------------------------------------------------------------------------------
// Export F_hat, normalized data to files for SVR
//-----------------------------------------------------------------------------------------------------

//const f1 = filename.

const st1 = filename.split("/");
const fname1 = st1[st1.length-1];
const st2 = missed_fname.split("/");
const fname2 = st2[st2.length-1];


const F_known_name = "./output/" +fname1+"_"+ fname2 + "_F_known_values_SVR.txt";
const F_unknown_name = "./output/" +fname1+"_"+ fname2 + "F_unknown_values_SVR.txt";
const F_hat_known_name = "./output/" +fname1+"_"+ fname2 +"_F_hat_known_values_SVR.txt";
const F_hat_unknown_name = "./output/"+fname1+"_"+ fname2 +"_F_hat_unknown_values_SVR.txt";
const sum_error_fname = "./output/" +fname1+"_"+ fname2 +"_errors.txt"; 

var F_known = require("fs");var F_unknown = require("fs");var F_hat_known = require("fs");var F_hat_unknown = require("fs");
var sum_error = require("fs");

var stream_F_known = F_known.createWriteStream(F_known_name, {flags:'a'});
var stream_F_unknown = F_unknown.createWriteStream(F_unknown_name, {flags:'a'});
var stream_F_hat_known = F_hat_known.createWriteStream(F_hat_known_name, {flags:'a'});
var stream_F_hat_unknown = F_hat_unknown.createWriteStream(F_hat_unknown_name, {flags:'a'});
var stream_sum_error = sum_error.createWriteStream(sum_error_fname, {flags:'a'});

console.log("Writing the files: "+ F_known_name + " " + F_unknown_name + " " + F_hat_known_name + " " +  F_hat_unknown_name);

for (let i=0;i<N ;i++ ) for (let j=0;j<N ;j++ )
	if (knownMat[i][j] != 0) {
		stream_F_known.write(F[i][j] + "\n");	
		stream_F_hat_known.write(F_hat[i][j] + "\n");}
	else{
		stream_F_unknown.write(data[i][j]/maxData + "\n");
		stream_F_hat_unknown.write(F_hat[i][j] + "\n");}

stream_F_known.end();stream_F_unknown.end();stream_F_hat_known.end();stream_F_hat_unknown.end();

stream_sum_error.write("d = 2, with height. INPUT files: " + filename + " + " +  missed_fname );
stream_sum_error.write("\nRoot Mean Square Error of TRAIN: " + RMSE);
stream_sum_error.write("\nMean Absolute Error of TRAIN: " + MAE);
stream_sum_error.write("\nRoot Mean Square Error of TEST: " + RMSE_test);
stream_sum_error.write("\nMean Absolute Error of TEST: " + MAE_test);
stream_sum_error.end(); 
console.log("\nAll done !!!");

/*
for plot in matlab

const F_name = "./output/" +missed_fname + "_F.txt";
const F_hat_name = "./output/"+missed_fname+"_F_hat.txt";
var F_ = require ("fs");
var F_hat_ = require("fs");
var stream_F_ = F_.createWriteStream(F_name, {flags:'a'});
var stream_F_hat_ = F_hat_.createWriteStream(F_hat_name, {flags:'a'});

for (let i=0;i<N ;i++ ) for (let j=0;j<N ;j++ )
	if (knownMat[i][j] != 0) 
		stream_F_.write(F[i][j] + "\n");	
	else
		stream_F_.write(data[i][j]/maxData + "\n");		
stream_F_.end();

for (let i=0;i<N ;i++ ) for (let j=0;j<N ;j++ ) stream_F_hat_.write(F_hat[i][j] + "\n");	

stream_F_hat_.end();
*/

