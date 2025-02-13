/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author kagan
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipesAddRemoveUpdate {
    
    
    private Connection con = null;
    
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    
    
    public ArrayList<Recipes> getRecipes(){
    
        ArrayList<Recipes> recipearraylist = new ArrayList<Recipes>();
        
        
        try {
            statement = con.createStatement();
            
            String query = "Select * From tarifler";
            
            ResultSet rs = statement.executeQuery(query);
            
        
            while(rs.next()){
            
                int id = rs.getInt("TarifID");
                String ad = rs.getString("TarifAdi");                    
                String kategori = rs.getString("Kategori");
                int sure = rs.getInt("HazirlamaSuresi");
                String talimatlar = rs.getString("Talimatlar");
                
                double maliyet = 0.0;
            try {
                String query1 = "SELECT ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                               "FROM tarifler t " +
                               "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                               "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                               "WHERE t.TarifAdi = ?";
                PreparedStatement preparedStatement = con.prepareStatement(query1);
                preparedStatement.setString(1, ad);
                ResultSet rs1 = preparedStatement.executeQuery();

                if (rs1.next()) {
                    maliyet = rs1.getDouble("ToplamMaliyet");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

                recipearraylist.add(new Recipes(id,ad,kategori,sure,talimatlar,maliyet));
                
      
            
            }
            
            return recipearraylist;
            
            
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    
    
    }
    
    
    public Double calculateMatchingPercentage(Recipes recipe , ArrayList<Integer> ingredients) {
    // Malzeme listesini SQL sorgusuna ekle
    ArrayList<Ingredients> ingredientList1 = new ArrayList<Ingredients>();
            
            
        for(Integer ingredient:ingredients){  
        ingredientList1.add(GetIngredientDetailsWithID(ingredient));
   
        }
        
    ArrayList<String> ingredientList = new ArrayList<String>();
    for(Ingredients ingredient : ingredientList1){
        ingredientList.add(ingredient.getAd());
    }
    String ingredientNames = String.join("','", ingredientList);
    String tarifadi = recipe.getAd();
    
    // Dinamik SQL sorgusu
    String query = "WITH MalzemeKarsilastirma AS ( " +
            "SELECT t.TarifID, t.TarifAdi, COUNT(*) AS EslesenMalzemeSayisi, " +
            "(SELECT COUNT(*) FROM tarifmalzemeleri WHERE TarifID = t.TarifID) AS ToplamMalzemeSayisi " +
            "FROM tarifler t " +
            "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
            "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
            "WHERE t.TarifAdi = ? " + // '?' kullanarak prepared statement ile güvenliği artırın
            "AND m.MalzemeAdi IN ('" + ingredientNames + "') " +
            "GROUP BY t.TarifID, t.TarifAdi ) " +
            "SELECT TarifAdi, " +
            "(EslesenMalzemeSayisi * 100.0 / ToplamMalzemeSayisi) AS EslesmeYuzdesi " +
            "FROM MalzemeKarsilastirma " +
            "ORDER BY EslesmeYuzdesi DESC";

    try {
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, tarifadi); // Tarif adını ekleyin
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            double eslesmeYuzdesi = rs.getDouble("EslesmeYuzdesi");
            return eslesmeYuzdesi; // Yüzdeyi döndür
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return null; // Eğer sonuç yoksa null döndür
}
    
    
    
    
    
    
    
    
    // Tarif ID'sine göre eksik malzeme maliyetini hesaplayan fonksiyon
    public double getEksikMalzemeMaliyeti(String recipeName) {
        double eksikMalzemeMaliyeti = 0.0;

        String query = "SELECT ROUND(SUM(CASE " +
                       "WHEN tm.MalzemeMiktar > m.ToplamMiktar THEN m.BirimFiyat * (tm.MalzemeMiktar - m.ToplamMiktar) " +
                       "ELSE 0 " +
                       "END),2) AS EksikMalzemeMaliyeti " +
                       "FROM tarifler t " +
                       "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                       "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                       "WHERE t.TarifAdi = ?";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, recipeName);  // recipeID parametresi burada kullanılıyor
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                eksikMalzemeMaliyeti = rs.getDouble("EksikMalzemeMaliyeti");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return eksikMalzemeMaliyeti;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
     
public ArrayList<Recipes> SearchWithName(String aramasonucu, String siralamaCB, String malzemesayisiCB, String maliyetaraligiCB, String kategoriCB) {
    ArrayList<Recipes> recipesWithNameSearchArrayList = new ArrayList<Recipes>();
    ArrayList<Recipes> filteredRecipes = new ArrayList<>();
    ArrayList<Recipes> finalFilteredRecipes = new ArrayList<>();
    String query = null;

    // İlk sorgu: Tarif adlarını al
    String initialQuery = "SELECT TarifAdi FROM tarifler WHERE TarifAdi LIKE ?";
    PreparedStatement preparedStatement;

    try {
        // İlk sorgu
        preparedStatement = con.prepareStatement(initialQuery);
        preparedStatement.setString(1, "%" + aramasonucu + "%");
        ResultSet rs = preparedStatement.executeQuery();

        // Alınan tarif adlarını listele
        ArrayList<String> tarifAdlari = new ArrayList<>();
        while (rs.next()) {
            String ad = rs.getString("TarifAdi");
            tarifAdlari.add(ad);
        }

        // Bu noktada tarifAdlari kullanılacak. Öncelikle sıralama yapılacak.
        ArrayList<Recipes> sortedRecipes = new ArrayList<>();

        if (!tarifAdlari.isEmpty() && !siralamaCB.equals("Seçiniz")) {
            String tarifListesi = String.join("','", tarifAdlari);

            // Sıralama şartlarına göre sorgu hazırla
            if (siralamaCB.equals("Ucuz")) {
                query = "SELECT t.TarifAdi, ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                        "FROM tarifler t " +
                        "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                        "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY ToplamMaliyet ASC"; 
            } else if (siralamaCB.equals("Pahalı")) {
                query = "SELECT t.TarifAdi, ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                        "FROM tarifler t " +
                        "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                        "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY ToplamMaliyet DESC"; 
            } else if (siralamaCB.equals("Hızlı")) {
                query = "SELECT t.TarifAdi, t.HazirlamaSuresi " +
                        "FROM tarifler t " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY t.HazirlamaSuresi ASC"; 
            } else if (siralamaCB.equals("Yavaş")) {
                query = "SELECT t.TarifAdi, t.HazirlamaSuresi " +
                        "FROM tarifler t " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY t.HazirlamaSuresi DESC"; 
            }

            // Sorguyu çalıştır ve sonuçları ekle
            preparedStatement = con.prepareStatement(query);
            ResultSet rsSorted = preparedStatement.executeQuery();
            while (rsSorted.next()) {
                String ad = rsSorted.getString("TarifAdi");
                sortedRecipes.add(new Recipes(ad)); // Eklemek istediğiniz diğer bilgilerle birlikte ekleyin
            }
        } else {
            // Eğer sıralama yoksa tarif adlarını direkt ekle
            for (String ad : tarifAdlari) {
                sortedRecipes.add(new Recipes(ad)); // Maliyet olmadan ekliyoruz
            }
        }

        // Malzeme sayısı filtresini uygula
        if (!malzemesayisiCB.equals("Seçiniz")) {
            int minMalzeme = 0, maxMalzeme = 20;

            switch (malzemesayisiCB) {
                case "0-3":
                    maxMalzeme = 3;
                    break;
                case "3-6":
                    minMalzeme = 4;
                    maxMalzeme = 6;
                    break;
                case "6-9":
                    minMalzeme = 7;
                    maxMalzeme = 9;
                    break;
                case "+9":
                    minMalzeme = 10; // 10 ve üzeri malzeme
                    break;
            }

            for (Recipes recipe : sortedRecipes) {
                String recipeName = recipe.getAd();
                // Malzeme sayısını kontrol etmek için sorgu oluştur
                String countQuery = "SELECT COUNT(*) AS MalzemeSayisi FROM tarifmalzemeleri tm " +
                                    "JOIN tarifler t ON tm.TarifID = t.TarifID " +
                                    "WHERE t.TarifAdi = ?";

                PreparedStatement countStatement = con.prepareStatement(countQuery);
                countStatement.setString(1, recipeName);
                ResultSet rsCount = countStatement.executeQuery();
                //System.out.println("MİNMALZEME" + minMalzeme + "MAXMALZEME" + maxMalzeme);
                if (rsCount.next()) {
                    int malzemeSayisi = rsCount.getInt("MalzemeSayisi");
                    if (malzemeSayisi >= minMalzeme && malzemeSayisi <= maxMalzeme) {
                        recipesWithNameSearchArrayList.add(recipe);
                    }
                }
            }
        } else {
            // Eğer malzeme sayısı filtresi yoksa tüm tarifleri ekle
            recipesWithNameSearchArrayList.addAll(sortedRecipes);
        }
        // Maliyet aralığı filtresini uygula
        if (!maliyetaraligiCB.equals("Seçiniz")) {
            double minMaliyet = 0, maxMaliyet = 1000;
            maliyetaraligiCB = maliyetaraligiCB.replace("₺", "").trim();
            switch (maliyetaraligiCB) {
                case "50 altı":
                    maxMaliyet = 50;
                    break;
                case "50-100":
                    minMaliyet = 50;
                    maxMaliyet = 100;
                    break;
                case "100-200":
                    minMaliyet = 100;
                    maxMaliyet = 200;
                    break;
                case "200 üstü":
                    minMaliyet = 200;
                    break;
                 default:
            System.out.println("Geçersiz maliyet aralığı seçimi.");
            }
            //System.out.println("Maliyet Araligi: " + minMaliyet + " - " + maxMaliyet);
            for (Recipes recipe : recipesWithNameSearchArrayList) {

                String costQuery = "SELECT ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                                   "FROM tarifler t " +
                                   "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                                   "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                                   "WHERE t.TarifAdi = ? " +
                                   "GROUP BY t.TarifID";

                PreparedStatement costStatement = con.prepareStatement(costQuery);
                costStatement.setString(1, recipe.getAd());
                ResultSet rsCost = costStatement.executeQuery();
                
                if (rsCost.next()) {
                    double toplamMaliyet = rsCost.getDouble("ToplamMaliyet");
                     //System.out.println("Toplam Maliyet: " + toplamMaliyet);
                     //System.out.println("Maliyet Aralığı: " + minMaliyet + " - " + maxMaliyet);

                    if (toplamMaliyet >= minMaliyet && toplamMaliyet < maxMaliyet) {
                        //System.out.println(recipe.getAd());
                        filteredRecipes.add(recipe);
                    }
                }
            }
        }
        else {
            // Eğer malzeme sayısı filtresi yoksa tüm tarifleri ekle
            filteredRecipes.addAll(recipesWithNameSearchArrayList);
        }
        // Kategori filtresini uygula
        if (!kategoriCB.equals("Seçiniz")) {
            kategoriCB = kategoriCB.substring(0, kategoriCB.length() - 3);

            for (Recipes recipe : filteredRecipes) {
                String recipeName = recipe.getAd();
                String categoryQuery = "SELECT t.Kategori FROM tarifler t WHERE t.TarifAdi = ?";

                PreparedStatement categoryStatement = con.prepareStatement(categoryQuery);
                categoryStatement.setString(1, recipeName);
                ResultSet rsCategory = categoryStatement.executeQuery();

                if (rsCategory.next()) {
                    String recipeCategory = rsCategory.getString("Kategori");
                    if (recipeCategory.equals(kategoriCB)) {
                        finalFilteredRecipes.add(recipe);
                    }
                }
            }
        }
        else{
        finalFilteredRecipes.addAll(filteredRecipes);
        }

        return finalFilteredRecipes;

    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
}

public ArrayList<Recipes> SearchWithIngredient(ArrayList<Recipes> recipeList, String siralamaCB, String malzemesayisiCB, String maliyetaraligiCB, String kategoriCB) {
    ArrayList<Recipes> recipesWithNameSearchArrayList = new ArrayList<Recipes>();
    ArrayList<Recipes> filteredRecipes = new ArrayList<>();
    ArrayList<Recipes> finalFilteredRecipes = new ArrayList<>();
    String query = null;

  
    try {

        // Alınan tarif adlarını listele
        ArrayList<String> tarifAdlari = new ArrayList<>();
        
        for(Recipes recipe:recipeList){
        
            tarifAdlari.add(recipe.getAd());
        
        }
    
        // Bu noktada tarifAdlari kullanılacak. Öncelikle sıralama yapılacak.
        ArrayList<Recipes> sortedRecipes = new ArrayList<>();

        if (!tarifAdlari.isEmpty() && !siralamaCB.equals("Seçiniz")) {
            String tarifListesi = String.join("','", tarifAdlari);

            // Sıralama şartlarına göre sorgu hazırla
            if (siralamaCB.equals("Ucuz")) {
                query = "SELECT t.TarifAdi, ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                        "FROM tarifler t " +
                        "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                        "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY ToplamMaliyet ASC"; 
            } else if (siralamaCB.equals("Pahalı")) {
                query = "SELECT t.TarifAdi, ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                        "FROM tarifler t " +
                        "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                        "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY ToplamMaliyet DESC"; 
            } else if (siralamaCB.equals("Hızlı")) {
                query = "SELECT t.TarifAdi, t.HazirlamaSuresi " +
                        "FROM tarifler t " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY t.HazirlamaSuresi ASC"; 
            } else if (siralamaCB.equals("Yavaş")) {
                query = "SELECT t.TarifAdi, t.HazirlamaSuresi " +
                        "FROM tarifler t " +
                        "WHERE t.TarifAdi IN ('" + tarifListesi + "') " +
                        "GROUP BY t.TarifID " +
                        "ORDER BY t.HazirlamaSuresi DESC"; 
            }

            // Sorguyu çalıştır ve sonuçları ekle
            preparedStatement = con.prepareStatement(query);
            ResultSet rsSorted = preparedStatement.executeQuery();
            while (rsSorted.next()) {
                String ad = rsSorted.getString("TarifAdi");
                sortedRecipes.add(new Recipes(ad)); // Eklemek istediğiniz diğer bilgilerle birlikte ekleyin
            }
        } else {
            // Eğer sıralama yoksa tarif adlarını direkt ekle
            for (String ad : tarifAdlari) {
                sortedRecipes.add(new Recipes(ad)); // Maliyet olmadan ekliyoruz
            }
        }

        // Malzeme sayısı filtresini uygula
        if (!malzemesayisiCB.equals("Seçiniz")) {
            int minMalzeme = 0, maxMalzeme = 20;

            switch (malzemesayisiCB) {
                case "0-3":
                    maxMalzeme = 3;
                    break;
                case "3-6":
                    minMalzeme = 4;
                    maxMalzeme = 6;
                    break;
                case "6-9":
                    minMalzeme = 7;
                    maxMalzeme = 9;
                    break;
                case "+9":
                    minMalzeme = 10; // 10 ve üzeri malzeme
                    break;
            }

            for (Recipes recipe : sortedRecipes) {
                String recipeName = recipe.getAd();
                // Malzeme sayısını kontrol etmek için sorgu oluştur
                String countQuery = "SELECT COUNT(*) AS MalzemeSayisi FROM tarifmalzemeleri tm " +
                                    "JOIN tarifler t ON tm.TarifID = t.TarifID " +
                                    "WHERE t.TarifAdi = ?";

                PreparedStatement countStatement = con.prepareStatement(countQuery);
                countStatement.setString(1, recipeName);
                ResultSet rsCount = countStatement.executeQuery();
                //System.out.println("MİNMALZEME" + minMalzeme + "MAXMALZEME" + maxMalzeme);
                if (rsCount.next()) {
                    int malzemeSayisi = rsCount.getInt("MalzemeSayisi");
                    if (malzemeSayisi >= minMalzeme && malzemeSayisi <= maxMalzeme) {
                        recipesWithNameSearchArrayList.add(recipe);
                    }
                }
            }
        } else {
            // Eğer malzeme sayısı filtresi yoksa tüm tarifleri ekle
            recipesWithNameSearchArrayList.addAll(sortedRecipes);
        }
        // Maliyet aralığı filtresini uygula
        if (!maliyetaraligiCB.equals("Seçiniz")) {
            double minMaliyet = 0, maxMaliyet = 1000;
            maliyetaraligiCB = maliyetaraligiCB.replace("₺", "").trim();
            switch (maliyetaraligiCB) {
                case "50 altı":
                    maxMaliyet = 50;
                    break;
                case "50-100":
                    minMaliyet = 50;
                    maxMaliyet = 100;
                    break;
                case "100-200":
                    minMaliyet = 100;
                    maxMaliyet = 200;
                    break;
                case "200 üstü":
                    minMaliyet = 200;
                    break;
                 default:
            System.out.println("Geçersiz maliyet aralığı seçimi.");
            }
            //System.out.println("Maliyet Araligi: " + minMaliyet + " - " + maxMaliyet);
            for (Recipes recipe : recipesWithNameSearchArrayList) {

                String costQuery = "SELECT ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet " +
                                   "FROM tarifler t " +
                                   "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                                   "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                                   "WHERE t.TarifAdi = ? " +
                                   "GROUP BY t.TarifID";

                PreparedStatement costStatement = con.prepareStatement(costQuery);
                costStatement.setString(1, recipe.getAd());
                ResultSet rsCost = costStatement.executeQuery();
                
                if (rsCost.next()) {
                    double toplamMaliyet = rsCost.getDouble("ToplamMaliyet");
                     //System.out.println("Toplam Maliyet: " + toplamMaliyet);
                     //System.out.println("Maliyet Aralığı: " + minMaliyet + " - " + maxMaliyet);

                    if (toplamMaliyet >= minMaliyet && toplamMaliyet < maxMaliyet) {
                        //System.out.println(recipe.getAd());
                        filteredRecipes.add(recipe);
                    }
                }
            }
        }
        else {
            // Eğer malzeme sayısı filtresi yoksa tüm tarifleri ekle
            filteredRecipes.addAll(recipesWithNameSearchArrayList);
        }
        // Kategori filtresini uygula
        if (!kategoriCB.equals("Seçiniz")) {
            kategoriCB = kategoriCB.substring(0, kategoriCB.length() - 3);

            for (Recipes recipe : filteredRecipes) {
                String recipeName = recipe.getAd();
                String categoryQuery = "SELECT t.Kategori FROM tarifler t WHERE t.TarifAdi = ?";

                PreparedStatement categoryStatement = con.prepareStatement(categoryQuery);
                categoryStatement.setString(1, recipeName);
                ResultSet rsCategory = categoryStatement.executeQuery();

                if (rsCategory.next()) {
                    String recipeCategory = rsCategory.getString("Kategori");
                    if (recipeCategory.equals(kategoriCB)) {
                        finalFilteredRecipes.add(recipe);
                    }
                }
            }
        }
        else{
        finalFilteredRecipes.addAll(filteredRecipes);
        }

        return finalFilteredRecipes;

    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
}




public ArrayList<Recipes> OrderbyPriceASC(ArrayList<Recipes> recipelist) {
    ArrayList<Recipes> sortedRecipesList = new ArrayList<>();
    
    // SQL sorgusu oluşturma
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT t.TarifAdi, ROUND(SUM(tm.MalzemeMiktar * m.BirimFiyat), 2) AS ToplamMaliyet ")
       .append("FROM tarifler t ")
       .append("JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID ")
       .append("JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID ")
       .append("WHERE t.TarifAdi IN (");

    // Liste elemanlarını virgülle ayırarak ekle
    for (int i = 0; i < recipelist.size(); i++) {
        sql.append("?");
        if (i < recipelist.size() - 1) {
            sql.append(", ");
        }
    }

    sql.append(") GROUP BY t.TarifAdi ORDER BY ToplamMaliyet ASC;");

    try {
        PreparedStatement preparedStatement = con.prepareStatement(sql.toString());

        // Parametreleri ekleme
        for (int i = 0; i < recipelist.size(); i++) {
            preparedStatement.setString(i + 1, recipelist.get(i).getAd()); // getAd() metodu tarif adını döndürmeli
        }

        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()) {
            String ad = rs.getString("TarifAdi");

            // `Recipes` sınıfına maliyet bilgisini ekle
            Recipes recipe = new Recipes(ad);
            sortedRecipesList.add(recipe);
        }

    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }
    for(Recipes recipes: recipelist ){
    
    
        System.out.println(recipes.getAd());
    
    
    }
    return sortedRecipesList; // Sıralanmış tarif listesini döndür
}
         
         
         
         
    
    
    
        public ArrayList<Ingredients> getIngredients(){
    
        ArrayList<Ingredients> ingredientarraylist = new ArrayList<Ingredients>();
        
        
        try {
            statement = con.createStatement();
            
            String query = "Select * From malzemeler";
            
            ResultSet rs = statement.executeQuery(query);
            
            
            while(rs.next()){
            
                int id = rs.getInt("MalzemeID");
                String ad = rs.getString("MalzemeAdi");                    
                String malzemebirim = rs.getString("MalzemeBirim");
                String miktar = rs.getString("ToplamMiktar");
                int fiyat = rs.getInt("BirimFiyat");
                
                ingredientarraylist.add(new Ingredients(id,ad,malzemebirim,miktar,fiyat));
            
            
            }
            
            return ingredientarraylist;
            
            
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    
    
    }
        
        
        
        
    public Ingredients GetIngredientDetails(String ad){
              
              Ingredients ingredient = new Ingredients();
             
            
        try {
            statement = con.createStatement();
           
                String query = "Select * From malzemeler where MalzemeAdi = ?";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, ad);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()){
                int id = resultSet.getInt("MalzemeID");
                String toplammiktar = resultSet.getString("ToplamMiktar");
                int birimfiyat = resultSet.getInt("BirimFiyat");
                String malzemebirim = resultSet.getString("MalzemeBirim");
                
                ingredient.setId(id);
                ingredient.setAd(ad);
                ingredient.setFiyat(birimfiyat);
                ingredient.setMalzemebirim(malzemebirim);
                ingredient.setMiktar(toplammiktar);
                
            return ingredient;
            
            
            }
       
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
          
            return null;
       
            
            }
    
    
    public Ingredients GetIngredientDetailsWithID(int id){
              
              Ingredients ingredient = new Ingredients();
             
            
        try {
            statement = con.createStatement();
           
                String query = "Select * From malzemeler where MalzemeID = ?";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, id);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()){
                String ad = resultSet.getString("MalzemeAdi");
                String toplammiktar = resultSet.getString("ToplamMiktar");
                int birimfiyat = resultSet.getInt("BirimFiyat");
                String malzemebirim = resultSet.getString("MalzemeBirim");
                
                ingredient.setId(id);
                ingredient.setAd(ad);
                ingredient.setFiyat(birimfiyat);
                ingredient.setMalzemebirim(malzemebirim);
                ingredient.setMiktar(toplammiktar);
                
            return ingredient;
            
            
            
            
            
            }
       
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
          
            return null;
       
            
            }
    
    
    
    
    
        
    
          public Recipes GetRecipeDetails(String ad){
              
              Recipes recipe = new Recipes();
             
            
        try {
            statement = con.createStatement();
           
            String query = "Select * From tarifler where TarifAdi = ?";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, ad);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()){
                String kategori = resultSet.getString("Kategori");
                int sure = resultSet.getInt("HazirlamaSuresi");
                String talimatlar = resultSet.getString("Talimatlar");
                String fotoUrl = resultSet.getString("FotoUrl");  // Fotoğraf URL'si sütununu alıyoruz

                
                recipe.setAd(ad);
                recipe.setKategori(kategori);
                recipe.setSure(sure);
                recipe.setTalimatlar(talimatlar);
                recipe.setFotoUrl(fotoUrl);
                
            return recipe;
            
            
            }   
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
           
            return null;
       
            
            }
          
          
          
          
          
          
          
          
          
          
            public Recipes GetRecipeDetailsWithID(int id){
              
              Recipes recipe = new Recipes();
             
            
        try {
            statement = con.createStatement();
           
                String query = "Select * From tarifler where TarifID = ?";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, id);
            
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if(resultSet.next()){
                String kategori = resultSet.getString("Kategori");
                int sure = resultSet.getInt("HazirlamaSuresi");
                String talimatlar = resultSet.getString("Talimatlar");
                String ad = resultSet.getString("TarifAdi");
                
                recipe.setAd(ad);
                recipe.setKategori(kategori);
                recipe.setSure(sure);
                recipe.setTalimatlar(talimatlar);
                
            return recipe;
            
            
            }   
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
           
            return null;
       
            
            }
    
    
    
    
    
    
public void UpdateRecipes(int id, String yeni_ad, String yeni_kategori, int yeni_sure, String yeni_talimatlar) {
    String query =  "UPDATE tarifler SET TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ? WHERE TarifID = ?";

    try {
        preparedStatement = con.prepareStatement(query);
        
        preparedStatement.setString(1, yeni_ad);
        preparedStatement.setString(2, yeni_kategori);
        preparedStatement.setInt(3, yeni_sure);
        preparedStatement.setString(4, yeni_talimatlar);
        preparedStatement.setInt(5, id);
        
        preparedStatement.executeUpdate();
        
    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    
    
        
            public void RemoveRecipes (int id) {
        
        String query = "Delete from tarifler where TarifID = ?";
        
        try {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, id);
            
            preparedStatement.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        
    }
            
            
            public boolean isRecipeExists(String ad) {
    boolean exists = false;
    try {
        
        String query = "SELECT * FROM tarifler WHERE TarifAdi = ?";
        preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, ad);
        
        ResultSet resultSet = preparedStatement.executeQuery();
        
        if (resultSet.next()) {
            exists = true;
        }
        
    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return exists;
}
            
                public boolean isIngredientExists(String ad) {
    boolean exists = false;
    try {
        
        String query = "SELECT * FROM malzemeler WHERE MalzemeAdi = ?";
        preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, ad);
        
        ResultSet resultSet = preparedStatement.executeQuery();
        
        if (resultSet.next()) {
            exists = true;
        }
        
    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return exists;
}        
            
            
                
                
                
            
      
            
            
            
            
         public void AddRecipes(String ad,String kategori,int sure,String talimatlar,String fotoUrl) {
        
        String query = "Insert Into tarifler (TarifAdi,Kategori,HazirlamaSuresi,Talimatlar,FotoUrl) VALUES(?,?,?,?,?)";
        
        
        if(isRecipeExists(ad)){
        
        
        
        }else{
            
             
        try {
            preparedStatement = con.prepareStatement(query);
            
            
            preparedStatement.setString(1, ad);
            preparedStatement.setString(2, kategori);
            preparedStatement.setInt(3, sure);
            preparedStatement.setString(4, talimatlar);
            preparedStatement.setString(5, fotoUrl);
            
            preparedStatement.executeUpdate();
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
            
            
        
        
        
        
        }
        
        
    
         }
         
         
         public void AddRecipeIngredients(int tarifID, int malzemeID, double malzemeMiktar) {
    // tarifmalzemeleri tablosuna tarif ve malzeme ilişkisini ekleyen SQL sorgusu
    String query = "INSERT INTO tarifmalzemeleri (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";

    try {
        preparedStatement = con.prepareStatement(query);
        preparedStatement.setInt(1, tarifID); // ilgili tarifin ID'si
        preparedStatement.setInt(2, malzemeID); // ilgili malzemenin ID'si
        preparedStatement.setDouble(3, malzemeMiktar); // malzeme miktarı
        
        preparedStatement.executeUpdate();
        System.out.println("Malzeme tarife başarıyla eklendi.");
    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        System.out.println("Malzeme tarife eklenirken bir hata oluştu.");
    }
}
         
         
         
         
        public void AddIngredients(String ad,String birim,int fiyat,String miktar) {
        
        String query = "Insert Into malzemeler (MalzemeAdi,ToplamMiktar,MalzemeBirim,BirimFiyat) VALUES(?,?,?,?)";
        
        
        if(isIngredientExists(ad)){
        
        
        
        }else{
            
             
        try {
            preparedStatement = con.prepareStatement(query);
            
            
            preparedStatement.setString(1, ad);
            preparedStatement.setString(2, miktar);
            preparedStatement.setString(3, birim);
            preparedStatement.setInt(4, fiyat);
            
            preparedStatement.executeUpdate();
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
            
            
        
        
        
        
        }
        
        
    
         }
        
        
   public ArrayList<Integer> getRecipesWithAllIngredients(ArrayList<Integer> ingredientIds) {
    ArrayList<Integer> recipeIds = new ArrayList<>();

    if (ingredientIds == null || ingredientIds.isEmpty()) {
        return recipeIds; // Eğer malzeme listesi boşsa boş bir liste döndür
    }

    String query = "SELECT tm.TarifID " +
                   "FROM tarifmalzemeleri tm " +
                   "WHERE tm.MalzemeID IN (" + generatePlaceholders(ingredientIds.size()) + ") " +
                   "GROUP BY tm.TarifID " +
                   "HAVING COUNT(DISTINCT tm.MalzemeID) = ?";

    try (PreparedStatement stmt = con.prepareStatement(query)) {
        // Malzeme ID'lerini sorguya ekle
        int index = 1;
        for (Integer ingredientId : ingredientIds) {
            stmt.setInt(index++, ingredientId);
        }

        // Malzeme sayısını HAVING kısmına ekle
        stmt.setInt(index, ingredientIds.size());

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                recipeIds.add(rs.getInt("TarifID"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();  // Hata yönetimi
    }

    return recipeIds;
}

// Yardımcı fonksiyon: '?' placeholder'ları oluşturmak için
private String generatePlaceholders(int count) {
    return String.join(", ", java.util.Collections.nCopies(count, "?"));
}
        
        
        
    public ArrayList<Ingredients> getIngredientsByRecipeName(String recipeName) {
    ArrayList<Ingredients> ingredientArrayList = new ArrayList<>();
    
    String query = "SELECT m.MalzemeAdi, tm.MalzemeMiktar, m.MalzemeBirim, m.BirimFiyat " +
                   "FROM tarifler t " +
                   "JOIN tarifmalzemeleri tm ON t.TarifID = tm.TarifID " +
                   "JOIN malzemeler m ON tm.MalzemeID = m.MalzemeID " +
                   "WHERE t.TarifAdi = ?";
    
    try {
        preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, recipeName);
        
        ResultSet rs = preparedStatement.executeQuery();
        
        while (rs.next()) {
            String malzemeAdi = rs.getString("MalzemeAdi");
            String malzemeMiktar = rs.getString("MalzemeMiktar");
            String malzemeBirim = rs.getString("MalzemeBirim");
            int birimFiyat = rs.getInt("BirimFiyat");
            
            Ingredients ingredient = new Ingredients(malzemeAdi, malzemeMiktar, malzemeBirim, birimFiyat);
            ingredientArrayList.add(ingredient);
        }
        
    } catch (SQLException ex) {
        Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return ingredientArrayList;
}
        
         
         
         
         
         
         public RecipesAddRemoveUpdate(){
         
         
         String url = "jdbc:mysql://" + DB.host + ":" + DB.port + "/" + DB.dbname+ "?useUnicode=true&characterEncoding=utf8";
         
         
         
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            con = DriverManager.getConnection(url, DB.username, DB.password);
        } catch (SQLException ex) {
            Logger.getLogger(RecipesAddRemoveUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         
         
         
         
         }
         
         
         
         
    
    
    
    
}
