function B=FindNuclearNormApprox(A, lambda)
% Finds the best LS approximation X, to a givem matrix M, such that
% ||X||<=lambda. The norm is the nuclear norm (sum of singular values).
    [u,s,v]=svd(A,'econ');
    sig = diag(s);
    if sum(sig)<lambda
        B=A;
        return;
    end
    n = length(sig);
    A = ones(1,n);
    %A = [ones(1,n-2) 100000 100000];
    b = lambda;
    H = eye(n);
    f = -sig(:);
    lb = zeros(n,1);
    x = quadprog(H,f,A,b,[],[],lb,[],[],optimset('Display','off'));
    %x = qpas(H,f,[],[],A,b,lb,[],0);
    B = u*diag(x)*v';
end