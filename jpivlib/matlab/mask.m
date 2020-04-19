function mask(inputFile,outputFile, maskFile)
% This program applies a mask to a jvc file imported from JPIV
% inputfile and output file are defined in the .jsc file
% 'maskname' is the name of the mask file 
% create the mask file using ImageJ 
% Select required area, then edit>selection>create mask
%% Import Data
vdata=load(inputFile);
x=vdata(:,1);
y=vdata(:,2);
u=vdata(:,3);
v=vdata(:,4);
s=vdata(:,5);
M=double(imread(maskFile));             % Import Mask file
M=M'/255;                               % transpose mask and binarise
%% Get number of vectors
% Get number of x and y vectors - searches through y column for a change in number. 
% This gives number of x co-ordinates, nox. When the number changes, the
% difference, k,  is found. Noy is found by subtracting the co-ordinate of the
% first vector from the last, then dividing by k and adding 1
for i=2:size(y)
    if y(i)>y(i-1)
        nox=i-1;
        k=y(i)-y(i-1);
        break
    end
end
noy=(y(end)-y(1))/k+1;
%% Convert Vectors to Matrices
U=reshape(u,nox,noy);
V=reshape(v,nox,noy);
X=reshape(x,nox,noy);
Y=reshape(y,nox,noy);
S=reshape(s,nox,noy);
%%  Apply Mask
t=0;                                %initialise
Umask=zeros(nox,noy);
Vmask=zeros(nox,noy);
Smask=zeros(nox,noy);
for i=1:nox
    for j=1:noy
        xc=X(i,j);                  % get x co-ordinate from X matrix
        yc=Y(i,j);                  % get y ....
        m=M(xc,yc);
        Umask(i,j)=m*U(i,j); % Multiply velocity component by binary mask
        Vmask(i,j)=m*V(i,j); %
        Smask(i,j)=m*S(i,j); %
        if m==0;
            t=t+1;
        end
    end
end
%sprintf(strcat(num2str(t),' vectors removed'))
%% Plot
% subplot(1,2,1)
% title('Masked')
% quiver(X,Y,Umask,Vmask);
% set(gca,'YDir','reverse')
% subplot(1,2,2)
% title('Original')
% quiver(X,Y,U,V);
% set(gca,'YDir','reverse')
%% Reformat into .jvc file
um=reshape(Umask,size(x,1),1);
vm=reshape(Vmask,size(x,1),1);
sm=reshape(Smask,size(x,1),1);
dataOut=[x y um vm sm];
save(outputFile, 'dataOut', '-ASCII');
