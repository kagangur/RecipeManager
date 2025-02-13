/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author kagan
 */
public class Recipes {

    public Recipes() {
    }
    
    
    private int id;
    private String ad;
    private String kategori;
    private int sure;
    private String talimatlar;
    private  double maliyet;
    private double uyum;
    private String fotoUrl;

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public double getUyum() {
        return uyum;
    }

    public void setUyum(double uyum) {
        this.uyum = uyum;
    }

    public Recipes(int id, String ad, String kategori, int sure, String talimatlar, double maliyet, double uyum) {
        this.id = id;
        this.ad = ad;
        this.kategori = kategori;
        this.sure = sure;
        this.talimatlar = talimatlar;
        this.maliyet = maliyet;
        this.uyum = uyum;
    }


    public Recipes(int id, String ad, String kategori, int sure, String talimatlar, double maliyet) {
        this.id = id;
        this.ad = ad;
        this.kategori = kategori;
        this.sure = sure;
        this.talimatlar = talimatlar;
        this.maliyet = maliyet;
    }
    
    
    public double getMaliyet() {
        return maliyet;
    }

    public void setMaliyet(double maliyet) {
        this.maliyet = maliyet;
    }

    public Recipes(String ad) {
        this.ad = ad;
    }
    
    
    
    

    public Recipes(int id, String ad, String kategori, int sure, String talimatlar) {
        this.id = id;
        this.ad = ad;
        this.kategori = kategori;
        this.sure = sure;
        this.talimatlar = talimatlar;
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

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public int getSure() {
        return sure;
    }

    public void setSure(int sure) {
        this.sure = sure;
    }

    public String getTalimatlar() {
        return talimatlar;
    }

    public void setTalimatlar(String talimatlar) {
        this.talimatlar = talimatlar;
    }
    
    
    
    
    
}
