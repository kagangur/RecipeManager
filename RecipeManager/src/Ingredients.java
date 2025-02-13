/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author kagan
 */
public class Ingredients {

    public Ingredients() {
    }
    
    
    private int id;
    private String ad;
    private String malzemebirim;
    private String miktar;
    private int fiyat;
    

    public Ingredients(int id, String ad, String malzemebirim, String miktar, int fiyat) {
        this.id = id;
        this.ad = ad;
        this.malzemebirim = malzemebirim;
        this.miktar = miktar;
        this.fiyat = fiyat;
    }

    public Ingredients(String malzemeAdi, String malzemeMiktar, String malzemeBirim, int birimFiyat) {
       this.ad=malzemeAdi;
       this.miktar=malzemeMiktar;
       this.malzemebirim=malzemeBirim;
       this.fiyat=birimFiyat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getMalzemebirim() {
        return malzemebirim;
    }

    public void setMalzemebirim(String malzemebirim) {
        this.malzemebirim = malzemebirim;
    }

    public String getMiktar() {
        return miktar;
    }

    public void setMiktar(String miktar) {
        this.miktar = miktar;
    }

    public int getFiyat() {
        return fiyat;
    }

    public void setFiyat(int fiyat) {
        this.fiyat = fiyat;
    }
    
    
}
