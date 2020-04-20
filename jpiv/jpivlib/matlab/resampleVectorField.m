function resampleVecorField( inputFile, outputFile, phi, x0, y0, dx, dy, nx, ny )
% resampleVecorField( inputFile, outputFile, phi, x0, y0, dx, dy, nx, ny )
% Rotates a vector field by 'angle' and interpolates ('cubic') the result on a regular grid.
% The vector field should contain five columns (x, y, u, v, s).
% inputFile: path of an ASCII file that contains the vector field data
% outputFile: filename for output
% phi: rotation angle in radiants, counterclockwise positive
% x0, y0: start point of regular grid
% dx, dy: increments of regular grid
% nx, ny: number of regular grid points in x and y direction

% read input data
data = load(inputFile);
x = data(:,1);
y = data(:,2);
u = data(:,3);
v = data(:,4);
s = data(:,5);
% translation of origin to the middle of the field
xt = x - x0 - (nx-1)/2*dx;
yt = y - y0 - (ny-1)/2*dy;
% rotation
xtr = xt * cos(phi) - yt * sin(phi);
ytr = xt * sin(phi) + yt * cos(phi);
ur = u * cos(phi) - v * sin(phi);
vr = u * sin(phi) + v * cos(phi);
% translation back to origin
xr = xtr + x0 + (nx-2)/2*dx;
yr = ytr + y0 + (ny-1)/2*dy;
% generation of regular grid
[X, Y] = meshgrid(x0:dx:x0+dx*(nx-1), y0:dy:y0+dy*(ny-1));
% interpolation on regular grid
U = griddata(xr, yr, ur, X, Y, 'linear');
V = griddata(xr, yr, vr, X, Y, 'linear');
S = griddata(xr, yr, s, X, Y, 'linear');
% replacing NaN by zeros
nanLocation = find(isnan(U));
U(nanLocation) = 0;
nanLocation = find(isnan(V));
V(nanLocation) = 0;
nanLocation = find(isnan(S));
S(nanLocation) = 0;
% convert matrices to column vectors
x = reshape(X',nx*ny,1);
y = reshape(Y',nx*ny,1);
u = reshape(U',nx*ny,1);
v = reshape(V',nx*ny,1);
s = reshape(S,nx*ny,1);
% write output file
dataOut = [x, y, u, v, s];
save(outputFile, 'dataOut', '-ASCII');