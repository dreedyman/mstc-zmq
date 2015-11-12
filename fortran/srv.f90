PROGRAM SRV

  USE JSON_MODULE
  USE ZMQ
  IMPLICIT NONE

  INTEGER(KIND = C_SIZE_T), PARAMETER :: METHOD_SIZE = 8_C_SIZE_T
  INTEGER(KIND = C_SIZE_T), PARAMETER :: BUFFER_SIZE = 1024_C_SIZE_T

  TYPE(C_PTR) :: CONTEXT
  TYPE(C_PTR) :: SOCKET
  TYPE(JSON_FILE) :: ARGUMENTS
  TYPE(JSON_FILE) :: RESULT
  INTEGER(KIND = C_INT) :: CODE
  INTEGER(KIND = C_INT) :: NBYTES
  CHARACTER(KIND = C_CHAR, LEN = METHOD_SIZE) :: METHOD
  CHARACTER(KIND = C_CHAR, LEN = BUFFER_SIZE), TARGET :: BUFFER
  CHARACTER(KIND = C_CHAR, LEN = :), TARGET, ALLOCATABLE :: JSON

  ! Initialize the JSON library.
  CALL JSON_INITIALIZE()

  ! Create a ZMQ socket and bind the socket to a TCP port.
  CONTEXT = ZMQ_CTX_NEW()
  SOCKET = ZMQ_SOCKET(CONTEXT, ZMQ_REP)
  CODE = ZMQ_BIND(SOCKET, 'tcp://*:5555')

  ! Respond to requests until the end of time.
  DO WHILE (.TRUE.)

    ! Receive the method name off of the socket.
    NBYTES = ZMQ_RECV(SOCKET, C_LOC(BUFFER), METHOD_SIZE, 0)
    METHOD = BUFFER(1:NBYTES)

    ! Receive the arguments off of the socket, in JSON format.
    NBYTES = ZMQ_RECV(SOCKET, C_LOC(BUFFER), BUFFER_SIZE, 0)
    CALL ARGUMENTS%LOAD_FROM_STRING(BUFFER(1:NBYTES))

    ! Execute the appropriate function based on the method name.
    SELECT CASE (METHOD)
      CASE ("isprime")
        RESULT = ISPRIME(ARGUMENTS)
      CASE ("average")
        RESULT = AVERAGE(ARGUMENTS)
      CASE DEFAULT
        CALL RESULT%LOAD_FROM_STRING('{"error": "unsupported-method"}')
    END SELECT

    ! Send the result to the socket, in JSON format.
    CALL RESULT%PRINT_TO_STRING(JSON)
    NBYTES = ZMQ_SEND(SOCKET, C_LOC(JSON), LEN(JSON, KIND = C_SIZE_T), 0)

    ! Release resources.
    DEALLOCATE(JSON)
    CALL RESULT%DESTROY()
    CALL ARGUMENTS%DESTROY()

  END DO

  ! Close the ZMQ socket.
  CODE = ZMQ_CLOSE(SOCKET)
  CODE = ZMQ_CTX_TERM(CONTEXT)

CONTAINS

  FUNCTION ISPRIME(IN) RESULT(OUT)
    TYPE(JSON_FILE), INTENT(INOUT) :: IN
    TYPE(JSON_FILE) :: OUT

    LOGICAL :: FOUND
    INTEGER :: VALUE

    CALL IN%GET('value', VALUE, FOUND)
    CALL OUT%LOAD_FROM_STRING('{}')
    CALL OUT%UPDATE('result', PRIMALITY_TESTER(VALUE), FOUND)
  END FUNCTION

  FUNCTION AVERAGE(IN) RESULT(OUT)
    TYPE(JSON_FILE), INTENT(INOUT) :: IN
    TYPE(JSON_FILE) :: OUT

    LOGICAL :: FOUND
    REAL(KIND = C_DOUBLE), DIMENSION(:), ALLOCATABLE :: VALUES

    CALL IN%GET('values', VALUES, FOUND)
    CALL OUT%LOAD_FROM_STRING('{}')
    CALL OUT%UPDATE('result', SUM(VALUES) / SIZE(VALUES), FOUND)

    DEALLOCATE(VALUES)
  END FUNCTION

  ! Naive primality test taken from pseudocode published in:
  !
  ! "Primality test." Wikipedia: The Free Encyclopedia. Wikimedia
  !     Foundation, Inc. 05 Oct. 2015. Web. 09 Oct. 2015.
  !
  FUNCTION PRIMALITY_TESTER(N) RESULT(PRIME)
    INTEGER, INTENT(IN) :: N
    LOGICAL :: PRIME

    INTEGER :: I

    IF (N <= 1) THEN
      PRIME = .FALSE.
      RETURN
    ELSE IF (N <= 3) THEN
      PRIME = .TRUE.
      RETURN
    ELSE IF (MOD(N, 2) == 0 .OR. MOD(N, 3) == 0) THEN
      PRIME = .FALSE.
      RETURN
    END IF

    I = 5
    DO WHILE (I * I <= N)
      IF (MOD(N, I) == 0 .OR. MOD(N, I + 2) == 0) THEN
        PRIME = .FALSE.
        RETURN
      END IF
      I = I + 6
    END DO

    PRIME = .TRUE.
  END FUNCTION

END PROGRAM
