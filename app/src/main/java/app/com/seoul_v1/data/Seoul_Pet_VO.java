package app.com.seoul_v1.data;

public class Seoul_Pet_VO {

    public String name ; //NM
    public String addr_old; // ADDR_OLD
    public String addr; // ADDR
    public String state; // STATE,영업상태,"운영중"
    public String tel; //TEL

    @Override
    public String toString() {
        return "Seoul_Pet_VO{" +
                "name='" + name + '\'' +
                ", addr_old='" + addr_old + '\'' +
                ", addr='" + addr + '\'' +
                ", state='" + state + '\'' +
                ", tel='" + tel + '\'' +
                '}';
    }
}
