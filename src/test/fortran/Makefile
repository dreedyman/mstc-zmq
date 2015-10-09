FC = gfortran
FFLAGS = -O2 -I /usr/local/lib -I ${HOME}/Documents/Temp/json-fortran/jsonfortran-gnu-4.2.0/lib
LDFLAGS = -L /usr/local/lib -L ${HOME}/Documents/Temp/json-fortran/jsonfortran-gnu-4.2.0/lib
LIBS = -ljsonfortran -lzmq

all:
	$(FC) $(FFLAGS) -c ../zmq.f90
	$(FC) $(FFLAGS) -c ../srv.f90
	$(FC) $(LDFLAGS) -o srv *.o $(LIBS)

clean:
	rm -rf *.o
	rm -rf *.mod
