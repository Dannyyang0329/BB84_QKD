import java.util.Random;

public class BB84 
{
    private final static int BITS_LENGTH = 60;
    private final static char Z_POLARIZER = '+';
    private final static char X_POLARIZER = 'x';

    private final static char X_BASIS_ONE = '\\';
    private final static char X_BASIS_ZERO = '/';
    private final static char Z_BASIS_ONE = '-';
    private final static char Z_BASIS_ZERO = '|';

    private final static int KEY_SHARE_SIZE = 10;   // ~95% detected


    public static void main(String[] args) 
    {
        /* Step 1. Alice chooses a string of random bits and a random choice of basis for each bit */
        // Alice chooses a string of random bits
        int[] aliceBits = generateRandomBitStream(BITS_LENGTH);  
        printAliceBitStream(aliceBits); 

        // Alice chooses some random basis
        char[] aliceBasis = generateRandomBasis(BITS_LENGTH);   
        printBasis(aliceBasis, "Alice");

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBasis     |                   |
         */


        /* Step 2. Alice encodes the bits and sends the encoded message to Bob */
        // ALice encodes the bits
        char[] encodedMessage = encode_message(aliceBits, aliceBasis);
        printEncodedMessage(encodedMessage);

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBasis     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message               
         */


        /* Step 3. If there is an eavesdropper , eve steals the encoded_message */
        Boolean isEveExist = (new Random().nextInt(2) == 0) ? true : false;

        if(isEveExist) {
            System.out.println("There is an eavesdropper.");

            // Eve uses random basis
            char[] eveBasis = generateRandomBasis(BITS_LENGTH);
            printBasis(eveBasis, "Eve");
            
            // Eve tries to measure the encoded_message
            char[] eveMeasureResult = measureEncodedMessage(encodedMessage, eveBasis);
            printMeasureMessage(eveMeasureResult);

            // Eve sends the encoded_message in order to hide herself
            encodedMessage = eveMeasureResult;

            System.out.println();
        }

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBasis     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         */
        

        /* Step 4. Bob measures the message at random basis */
        // Bob chooses random basis
        char[] bobBasis = generateRandomBasis(BITS_LENGTH);
        printBasis(bobBasis, "Bob");

        // Bob knows the result after measuring
        char[] bobMeasureResult = measureEncodedMessage(encodedMessage, bobBasis);
        printMeasureMessage(bobMeasureResult);

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBasis     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         *                     |                   |      bobBasis
         *                     |                   |  bobMeasureResult
         */

        
        /* Step 5. Alice and Bob publicly share thier basis */
        System.out.println("Alice shares the basis she used to Bob.");
        System.out.println("Bob shares the basis he used to Alice.");

        String aliceKey = generateKeyForAlice(aliceBasis, bobBasis, aliceBits);
        String bobKey = generateKeyForBob(aliceBasis, bobBasis, bobMeasureResult);

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBasis     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         *                     |                   |      bobBasis
         *                     |                   |  bobMeasureResult
         *                     |     aliceBasis    |     aliceBasis
         *      bobBasis       |      bobBasis     |               
         *      aliceKey       |                   |       bobKey
         */


        /* Step 6. Alice and Bob share a section of their key to determine that eavesdropper
         *         doesn't exist and the key is valid.
         */
        String bobSample = bobKey.substring(0, KEY_SHARE_SIZE);
        String aliceSample = aliceKey.substring(0, KEY_SHARE_SIZE);

        System.out.println("Bob's sample : \t\t" + bobSample);
        System.out.println("Alice's sample : \t" + aliceSample);
        System.out.println();

        if(bobSample.equals(aliceSample)) {
            System.out.println("Transmission successful");
        }
        else {
            System.out.println("There is an eavesdropper existed.");
            System.out.println("Transmission failed");
        }

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBasis     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         *                     |                   |      bobBasis
         *                     |                   |  bobMeasureResult
         *                     |     aliceBasis    |     aliceBasis
         *      bobBasis       |      bobBasis     |               
         *      aliceKey       |                   |       bobKey
         *      bobSample      |     bobSample     |      bobSample
         *     aliceSample     |    aliceSample    |     aliceSample  
         */
    }


    private static int[] generateRandomBitStream(int numOfBits)
    {
        Random rnd = new Random();

        int[] bits = new int[numOfBits];

        for(int i=0 ; i<numOfBits ; i++) bits[i] = rnd.nextInt(2);

        return bits;
    }

    private static void printAliceBitStream(int[] aliceBits) 
    {
        System.out.print("Alice's stream of bits : \t");
        for(int i=0 ; i<aliceBits.length; i++) {
            System.out.print(aliceBits[i] + " ");
        }
        System.out.println();
    }

    private static char[] generateRandomBasis(int numOfBasis) 
    {
        Random rnd = new Random();        

        char[] basis = new char[numOfBasis];

        for(int i=0 ; i<numOfBasis ; i++) {
            basis[i] = (rnd.nextInt(2) == 1) ? Z_POLARIZER : X_POLARIZER;
        } 

        return basis;
    }

    private static void printBasis(char[] basis, String name)
    {
        System.out.print(name + "'s basis is : \t\t");
        for(int i=0 ; i<basis.length ; i++) {
            System.out.print(basis[i] + " ");
        }
        System.out.println();
    }

    private static char[] encode_message(int[] bitStream, char[] basis)
    {
        char[] encodedMessage = new char[BITS_LENGTH];

        for(int i=0 ; i<encodedMessage.length ; i++) {
            if(basis[i] == Z_POLARIZER) {
                encodedMessage[i] = (bitStream[i] == 0) ? Z_BASIS_ZERO : Z_BASIS_ONE;
            }
            else if(basis[i] == X_POLARIZER) {
                encodedMessage[i] = (bitStream[i] == 0) ? X_BASIS_ZERO : X_BASIS_ONE;
            }
        }

        return encodedMessage;
    }

    private static void printEncodedMessage(char[] encodedMessage)
    {
        System.out.print("Encoded Message is : \t\t");
        for(int i=0 ; i<encodedMessage.length; i++) {
            System.out.print(encodedMessage[i] + " ");
        }
        System.out.println();
    }

    private static char[] measureEncodedMessage(char[] encodedMessage, char[] basis)
    {
        Random rnd = new Random();

        char[] measureResult = new char[BITS_LENGTH];

        for(int i=0 ; i<encodedMessage.length ; i++) {
            if(basis[i] == Z_POLARIZER) {
                if(encodedMessage[i] == Z_BASIS_ONE || encodedMessage[i] == Z_BASIS_ZERO) {
                    measureResult[i] = encodedMessage[i];
                }
                else measureResult[i] = (rnd.nextInt(2)%2 == 1) ? Z_BASIS_ONE : Z_BASIS_ZERO;
            }
            else if(basis[i] == X_POLARIZER) {
                if(encodedMessage[i] == X_BASIS_ONE || encodedMessage[i] == X_BASIS_ZERO) {
                    measureResult[i] = encodedMessage[i];
                }
                else measureResult[i] = (rnd.nextInt(2)%2 == 1) ? X_BASIS_ONE : X_BASIS_ZERO;
            }
        }

        return measureResult;
    }

    private static void printMeasureMessage(char[] measureMessage)
    {
        System.out.print("Measure Message is : \t\t");
        for(int i=0 ; i<measureMessage.length; i++) {
            System.out.print(measureMessage[i] + " ");
        }
        System.out.println();
    }

    private static String generateKeyForAlice(char[] aliceBasis, char[] bobBasis, int[] bitStream) 
    {
        String key = "";

        for(int i=0 ; i<BITS_LENGTH ; i++) {
            if(aliceBasis[i] == bobBasis[i]) {
                key += Integer.toString(bitStream[i]);
            }
        }

        return key;
    }

    private static String generateKeyForBob(char[] aliceBasis, char[] bobBasis, char[] bobMeasureResult) 
    {
        String key = "";

        for(int i=0 ; i<BITS_LENGTH ; i++) {
            if(aliceBasis[i] == bobBasis[i]) {
                switch(bobMeasureResult[i]) {
                    case Z_BASIS_ONE:
                        key += "1";
                        break;
                    case Z_BASIS_ZERO:
                        key += "0";
                        break;
                    case X_BASIS_ONE:
                        key += "1";
                        break;
                    case X_BASIS_ZERO:
                        key += "0";
                        break;
                }
            }
        }

        return key;
    }
}
