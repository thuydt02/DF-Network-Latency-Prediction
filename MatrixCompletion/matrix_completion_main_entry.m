% Demo for matrix completion
% Create a random matrix of low rank, remove values and complete.
%
clc
filename = 'PlanetLabData_1.csv';
A = csvread(filename);
%A = randn(200,10)*randn(10,200); % 200x200 of rank 10

imax = 10;
%A = randi(imax,10,2)*randi(imax,2,10); % 200x200 of rank 10
B = rand(size(A))< 0.8; % remove 10% of the entries
lamnbda_tol = 10;
tol = 1e-7;
N = 500; % number of iterations
fprintf('Completion using nuclear norm minimization... \n');
[CompletedMat, ier] = MatrixCompletion(A.*B, B,N, 'nuclear', lamnbda_tol, tol, 0);


fprintf('\n Corrupted matrix nuclear norm (initial): %g \n',sum(svd(A.*B)));
fprintf('Restored matrix nuclear norm (final): %g \n',sum(svd(CompletedMat)));
fprintf('MSE on known entries: %g \n',sqrt(sum2((CompletedMat-A).*B)/sum(B(:))));
%{
fprintf('\n Completion using spectral norm minimization... \n');
[CompletedMat1, ier1] = MatrixCompletion(A.*B, B,N, 'spectral', lamnbda_tol, tol, 0);


fprintf('Restored matrix spectral norm (final): %g \n',norm(CompletedMat1));
fprintf('MSE on known entries: %g \n',sqrt(sum2((CompletedMat1-A).*B)/sum(B(:))));
fprintf('\n Completion using weighted norm minimization (pushing to low rank, no global convergence)... \n');
[CompletedMat, ier] = MatrixCompletion(A.*B, B,N, 'NuclearWeighted', lamnbda_tol, tol, 0, [ones(1,10) ones(1,190)*10000] ); % big penalty on small singular values

fprintf('Corrupted matrix rank (initial): %g \n',rank(A.*B));
fprintf('Restored matrix rank (final): %g \n',rank(CompletedMat));
fprintf('MSE on known entries: %g \n \n',sqrt(sum2((CompletedMat-A).*B)));
%}