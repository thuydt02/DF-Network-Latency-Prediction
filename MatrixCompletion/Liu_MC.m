function [ret] = Liu_MC(A, B)
clc
lamnbda_tol = 10;
tol = 1e-7;
N = 1; % number of iterations = 500
fprintf('Completion using nuclear norm minimization... \n');
[CompletedMat, ier] = MatrixCompletion(A.*B, B,N, 'nuclear', lamnbda_tol, tol, 0);
ret = CompletedMat;
%fprintf('\n Corrupted matrix nuclear norm (initial): %g \n',sum(svd(A.*B)));
%fprintf('Restored matrix nuclear norm (final): %g \n',sum(svd(CompletedMat)));
%fprintf('MSE on known entries: %g \n',sqrt(sum2((CompletedMat-A).*B)/sum(B(:))));