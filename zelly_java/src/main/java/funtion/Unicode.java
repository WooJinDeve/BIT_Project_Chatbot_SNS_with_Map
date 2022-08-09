package funtion;

public class Unicode {
    private static final Unicode instance = new Unicode();

    private Unicode(){

    }

    public static Unicode getInstance(){
        return instance;
    }

    public String uniToKor(String uni){
        StringBuffer result = new StringBuffer();

        for(int i=0; i<uni.length(); i++){
            if(uni.charAt(i) == '\\' &&  uni.charAt(i+1) == 'u'){
                Character c = (char)Integer.parseInt(uni.substring(i+2, i+6), 16);
                result.append(c);
                i+=5;
            }else{
                result.append(uni.charAt(i));
            }
        }
        return result.toString();
    }

    public String korToUni(String kor){
        StringBuffer result = new StringBuffer();

        for(int i=0; i<kor.length(); i++){
            int cd = kor.codePointAt(i);
            if (cd < 128){
                result.append(String.format("%c", cd));
            }else{
                result.append(String.format("\\u%04x", cd));
            }
        }
        return result.toString();
    }
}
