/*
* date.x Specification of the remote date and time server
*/


/*
* Define two procedures
* bin_date_1() returns the binary date and time (no arguments)
* str_date_1() takes a binary time and returns a string
*
*/

typedef int arr[10];

program HW2 {
  version DATE_VERS {

    bool APPEND(arr) = 1;
    int QUERY(int)=2;
    bool REMOVE(int)=3;
    
  } = 1; /* version number = 1 */
} = 0x31234567; /* program number = 0x31234567 */
