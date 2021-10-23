import java.util.Random;

public class BB84 
{
    private final static int BITS_LENGTH = 60;
    private final static char Z_POLARIZER = '+';
    private final static char X_POLARIZER = 'x';

    private final static char X_BASES_ONE = '\\';
    private final static char X_BASES_ZERO = '/';
    private final static char Z_BASES_ONE = '-';
    private final static char Z_BASES_ZERO = '|';

    private final static int KEY_SHARE_SIZE = 10;   // ~95% detected


    public static void main(String[] args) 
    {
        Boolean isEveExist = (new Random().nextInt(2) == 0) ? true : false;

        /* Step 1. Alice chooses a string of random bits and a random choice of basis for each bit */
        // Alice chooses a string of random bits
        int[] aliceBits = generateRandomBitStream(BITS_LENGTH);  
        printAliceBitStream(aliceBits); 

        // Alice chooses some random bases
        char[] aliceBases = generateRandomBases(BITS_LENGTH);   
        printBases(aliceBases, "Alice");

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBases     |                   |
         */


        /* Step 2. Alice encodes the bits and sends the encoded message to Bob */
        // ALice encodes the bits
        char[] encodedMessage = encode_message(aliceBits, aliceBases);
        printEncodedMessage(encodedMessage);

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBases     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message               
         */


        /* Step 3. If there is an eavesdropper , eve steals the encoded_message */
        if(isEveExist) {
            System.out.println("There is an eavesdropper.");

            // Eve uses random bases
            char[] eveBases = generateRandomBases(BITS_LENGTH);
            printBases(eveBases, "Eve");
            
            // Eve tries to measure the encoded_message
            char[] eveMeasureResult = measureEncodedMessage(encodedMessage, eveBases);
            printMeasureMessage(eveMeasureResult);

            // Eve sends the encoded_message in order to hide herself
            encodedMessage = eveMeasureResult;

            System.out.println();
        }

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBases     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         */
        

        /* Step 4. Bob measures the message at random basis */
        // Bob chooses random bases
        char[] bobBases = generateRandomBases(BITS_LENGTH);
        printBases(bobBases, "Bob");

        // Bob knows the result after measuring
        char[] bobMeasureResult = measureEncodedMessage(encodedMessage, bobBases);
        printMeasureMessage(bobMeasureResult);

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBases     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         *                     |                   |      bobBases
         *                     |                   |  bobMeasureResult
         */

        
        /* Step 5. Alice and Bob publicly share thier basis */
        System.out.println("Alice shares the bases she used to Bob.");
        System.out.println("Bob shares the bases he used to Alice.");

        String aliceKey = generateKeyForAlice(aliceBases, bobBases, aliceBits);
        String bobKey = generateKeyForBob(aliceBases, bobBases, bobMeasureResult);

        System.out.println();

        /* Alice's acknowledge | Eve's acknowledge | Bob's acknowledge 
         *      aliceBits      |                   |
         *      aliceBases     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         *                     |                   |      bobBases
         *                     |                   |  bobMeasureResult
         *                     |     aliceBases    |     aliceBases
         *      bobBases       |      bobBases     |               
         *      aliceKey       |                   |       bobKey
         */


        /* Step 6. Alice and Bob share a section of their key to determine that eavesdropper
         *         doesn't exist and the key is valid.
         */
        String bobSample = bobKey.substring(KEY_SHARE_SIZE);
        String aliceSample = aliceKey.substring(KEY_SHARE_SIZE);

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
         *      aliceBases     |                   |
         *   encoded_message   |  encoded_message  |  encoded_message*               
         *                     |                   |      bobBases
         *                     |                   |  bobMeasureResult
         *                     |     aliceBases    |     aliceBases
         *      bobBases       |      bobBases     |               
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

    private static char[] generateRandomBases(int numOfBases) 
    {
        Random rnd = new Random();        

        char[] bases = new char[numOfBases];

        for(int i=0 ; i<numOfBases ; i++) {
            bases[i] = (rnd.nextInt(2) == 1) ? Z_POLARIZER : X_POLARIZER;
        } 

        return bases;
    }

    private static void printBases(char[] bases, String name)
    {
        System.out.print(name + "'s bases is : \t\t");
        for(int i=0 ; i<bases.length ; i++) {
            System.out.print(bases[i] + " ");
        }
        System.out.println();
    }

    private static char[] encode_message(int[] bitStream, char[] bases)
    {
        char[] encodedMessage = new char[BITS_LENGTH];

        for(int i=0 ; i<encodedMessage.length ; i++) {
            if(bases[i] == Z_POLARIZER) {
                encodedMessage[i] = (bitStream[i] == 0) ? Z_BASES_ZERO : Z_BASES_ONE;
            }
            else if(bases[i] == X_POLARIZER) {
                encodedMessage[i] = (bitStream[i] == 0) ? X_BASES_ZERO : X_BASES_ONE;
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

    private static char[] measureEncodedMessage(char[] encodedMessage, char[] bobBases)
    {
        Random rnd = new Random();

        char[] measureResult = new char[BITS_LENGTH];

        for(int i=0 ; i<encodedMessage.length ; i++) {
            if(encodedMessage[i] == Z_BASES_ONE || encodedMessage[i] == Z_BASES_ZERO) {
                if(bobBases[i] == Z_POLARIZER) measureResult[i] = encodedMessage[i];
                else {
                    measureResult[i] = (rnd.nextInt(2)%2 == 1) ? Z_BASES_ONE : Z_BASES_ZERO;
                }
            }
            else {
                if(bobBases[i] == X_POLARIZER) measureResult[i] = encodedMessage[i];
                else {
                    measureResult[i] = (rnd.nextInt(2)%2 == 1) ? X_BASES_ONE : X_BASES_ZERO;
                }
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

    private static String generateKeyForAlice(char[] aliceBases, char[] bobBases, int[] bitStream) 
    {
        String key = "";

        for(int i=0 ; i<BITS_LENGTH ; i++) {
            if(aliceBases[i] == bobBases[i]) {
                key += Integer.toString(bitStream[i]);
            }
        }

        return key;
    }

    private static String generateKeyForBob(char[] aliceBases, char[] bobBases, char[] bobMeasureResult) 
    {
        String key = "";

        for(int i=0 ; i<BITS_LENGTH ; i++) {
            if(aliceBases[i] == bobBases[i]) {
                switch(bobMeasureResult[i]) {
                    case Z_BASES_ONE:
                        key += "1";
                        break;
                    case Z_BASES_ZERO:
                        key += "0";
                        break;
                    case X_BASES_ONE:
                        key += "1";
                        break;
                    case X_BASES_ZERO:
                        key += "0";
                        break;
                }
            }
        }

        return key;
    }
}
