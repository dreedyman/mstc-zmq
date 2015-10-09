MODULE ZMQ

  USE, INTRINSIC :: ISO_C_BINDING
  IMPLICIT NONE

  TYPE, BIND(C) :: ZMQ_MSG_T
    INTEGER(KIND = C_SIGNED_CHAR), DIMENSION(64) :: D
  END TYPE

  INTEGER, PARAMETER :: ZMQ_REQ = 3
  INTEGER, PARAMETER :: ZMQ_REP = 4

  INTERFACE

    FUNCTION ZMQ_BIND(SOCKET, ENDPOINT) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_CHAR, C_INT, C_PTR
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: SOCKET
      CHARACTER(KIND = C_CHAR), DIMENSION(*), INTENT(IN) :: ENDPOINT
      INTEGER(KIND = C_INT) :: ZMQ_BIND
    END FUNCTION

    FUNCTION ZMQ_CLOSE(SOCKET) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_INT, C_PTR
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: SOCKET
      INTEGER(KIND = C_INT) :: ZMQ_CLOSE
    END FUNCTION

    FUNCTION ZMQ_CONNECT(SOCKET, ENDPOINT) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_CHAR, C_INT, C_PTR
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: SOCKET
      CHARACTER(KIND = C_CHAR), DIMENSION(*), INTENT(IN) :: ENDPOINT
      INTEGER(KIND = C_INT) :: ZMQ_CONNECT
    END FUNCTION

    FUNCTION ZMQ_CTX_NEW() BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_PTR
      IMPLICIT NONE
      TYPE(C_PTR) :: ZMQ_CTX_NEW
    END FUNCTION

    FUNCTION ZMQ_CTX_TERM(CONTEXT) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_INT, C_PTR
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: CONTEXT
      INTEGER(KIND = C_INT) :: ZMQ_CTX_TERM
    END FUNCTION

    FUNCTION ZMQ_RECV(SOCKET, BUF, LEN, FLAGS) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_INT, C_PTR, C_SIZE_T
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: SOCKET
      TYPE(C_PTR), VALUE, INTENT(IN) :: BUF
      INTEGER(KIND = C_SIZE_T), VALUE, INTENT(IN) :: LEN
      INTEGER(KIND = C_INT), VALUE, INTENT(IN) :: FLAGS
      INTEGER(KIND = C_INT) :: ZMQ_RECV
    END FUNCTION

    FUNCTION ZMQ_SEND(SOCKET, BUF, LEN, FLAGS) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_INT, C_PTR, C_SIZE_T
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: SOCKET
      TYPE(C_PTR), VALUE, INTENT(IN) :: BUF
      INTEGER(KIND = C_SIZE_T), VALUE, INTENT(IN) :: LEN
      INTEGER(KIND = C_INT), VALUE, INTENT(IN) :: FLAGS
      INTEGER(KIND = C_INT) :: ZMQ_SEND
    END FUNCTION

    FUNCTION ZMQ_SOCKET(CONTEXT, TYPE_) BIND(C)
      USE, INTRINSIC :: ISO_C_BINDING, ONLY : C_INT, C_PTR
      IMPLICIT NONE
      TYPE(C_PTR), VALUE, INTENT(IN) :: CONTEXT
      INTEGER(KIND = C_INT), VALUE, INTENT(IN) :: TYPE_
      TYPE(C_PTR) :: ZMQ_SOCKET
    END FUNCTION

  END INTERFACE

END MODULE