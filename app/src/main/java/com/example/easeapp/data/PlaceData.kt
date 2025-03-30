package com.example.ease.data

data class Place(
    val name: String,
    val address: String,
    val city: String,
    val phone: String,
    val workingHours: String,
    val description: String,
    val photoUrl: String,
    val lat: Double,
    val lng: Double
)

object PlaceRepository {
    val places: List<Place> = listOf(
        Place(
            name = "Tel Aviv Sourasky Medical Center (Ichilov)",
            address = "6 Weizmann St, Tel Aviv",
            city = "Tel Aviv",
            phone = "+972 3-6974444",
            workingHours = "24/7",
            description = "One of the largest hospitals in Israel, offering advanced healthcare, research, and education.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/IchilovMedicalCenterTelAviv.jpg/640px-IchilovMedicalCenterTelAviv.jpg",
            lat = 32.0809,
            lng = 34.7818
        ),
        Place(
            name = "Hadassah Medical Center – Ein Kerem",
            address = "Henrietta Szold St, Jerusalem",
            city = "Jerusalem",
            phone = "+972 2-6777111",
            workingHours = "24/7",
            description = "A university-affiliated hospital providing a wide range of medical services.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Hadassah_Ein_Kerem.jpg/640px-Hadassah_Ein_Kerem.jpg",
            lat = 31.7615,
            lng = 35.1456
        ),
        Place(
            name = "Rambam Health Care Campus",
            address = "8 HaHagana Ave, Haifa",
            city = "Haifa",
            phone = "+972 4-8542222",
            workingHours = "24/7",
            description = "A major medical center in northern Israel with specialization in trauma and advanced care.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Rambam_Medical_Center.jpg/640px-Rambam_Medical_Center.jpg",
            lat = 32.7984,
            lng = 34.9726
        ),
        Place(
            name = "Soroka University Medical Center",
            address = "151 Reger Blvd, Be'er Sheva",
            city = "Be'er Sheva",
            phone = "+972 8-6400111",
            workingHours = "24/7",
            description = "The largest hospital in southern Israel, providing comprehensive medical services.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/SorokaMedicalCenter.jpg/640px-SorokaMedicalCenter.jpg",
            lat = 31.2520,
            lng = 34.7915
        ),
        Place(
            name = "Shaare Zedek Medical Center",
            address = "12 Bayit St, Jerusalem",
            city = "Jerusalem",
            phone = "+972 2-6555111",
            workingHours = "24/7",
            description = "A major hospital in Jerusalem offering a wide range of medical treatments and services.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/ShaareZedek.jpg/640px-ShaareZedek.jpg",
            lat = 31.7726,
            lng = 35.1906
        ),
        Place(
            name = "Kaplan Medical Center",
            address = "Avraham Shapira St, Rehovot",
            city = "Rehovot",
            phone = "+972 8-9441111",
            workingHours = "24/7",
            description = "A major hospital serving central Israel, affiliated with the Hebrew University.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Kaplan_Medical_Center.jpg/640px-Kaplan_Medical_Center.jpg",
            lat = 31.8887,
            lng = 34.8071
        ),
        Place(
            name = "Barzilai Medical Center",
            address = "Barzilai St, Ashkelon",
            city = "Ashkelon",
            phone = "+972 8-6745555",
            workingHours = "24/7",
            description = "A regional medical center in Ashkelon, providing a wide range of services.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/32/Barzilai_medical_center_2017.jpg/640px-Barzilai_medical_center_2017.jpg",
            lat = 31.6682,
            lng = 34.5712
        ),
        Place(
            name = "Ziv Medical Center",
            address = "Safed, Northern District",
            city = "Safed",
            phone = "+972 4-6828811",
            workingHours = "24/7",
            description = "A northern hospital serving the Galilee region with trauma and general medical care.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a6/Ziv_Medical_Center.jpg/640px-Ziv_Medical_Center.jpg",
            lat = 32.9657,
            lng = 35.4983
        ),
        Place(
            name = "Meir Medical Center",
            address = "Tchernichovsky St, Kfar Saba",
            city = "Kfar Saba",
            phone = "+972 9-7472555",
            workingHours = "24/7",
            description = "A large hospital in the Sharon region, known for its respiratory and rehabilitation centers.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Meir_Hospital_Kfar_Saba.jpg/640px-Meir_Hospital_Kfar_Saba.jpg",
            lat = 32.1846,
            lng = 34.9071
        ),
        Place(
            name = "Sheba Medical Center (Tel HaShomer)",
            address = "Tel HaShomer, Ramat Gan",
            city = "Ramat Gan",
            phone = "+972 3-5303030",
            workingHours = "24/7",
            description = "Israel's largest hospital, globally recognized for its medical innovation and research.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Sheba_Tel_HaShomer_Medical_Center.jpg/640px-Sheba_Tel_HaShomer_Medical_Center.jpg",
            lat = 32.0482,
            lng = 34.8413
        ),
        Place(
            name = "Beilinson Hospital (Rabin Medical Center)",
            address = "Jabotinsky St, Petah Tikva",
            city = "Petah Tikva",
            phone = "+972 3-9376666",
            workingHours = "24/7",
            description = "A leading hospital in Israel known for its cardiac and oncology departments.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/42/Beilinson_Hospital_Rabin_Medical_Center.jpg/640px-Beilinson_Hospital_Rabin_Medical_Center.jpg",
            lat = 32.0911,
            lng = 34.8714
        ),
        Place(
            name = "Schneider Children's Medical Center",
            address = "Kaplan St, Petah Tikva",
            city = "Petah Tikva",
            phone = "+972 3-9253655",
            workingHours = "24/7",
            description = "Israel’s national pediatric hospital, offering comprehensive child healthcare.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Schneider_Children%27s_Medical_Center.jpg/640px-Schneider_Children%27s_Medical_Center.jpg",
            lat = 32.0927,
            lng = 34.8753
        ),
        Place(
            name = "Wolfson Medical Center",
            address = "Halohamim St, Holon",
            city = "Holon",
            phone = "+972 3-5028211",
            workingHours = "24/7",
            description = "A major hospital serving the Holon and southern Tel Aviv area.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/Wolfson_Hospital_Holon.jpg/640px-Wolfson_Hospital_Holon.jpg",
            lat = 32.0242,
            lng = 34.7680
        ),
        Place(
            name = "Herzliya Medical Center",
            address = "Ramat Yam St, Herzliya",
            city = "Herzliya",
            phone = "+972 9-9592555",
            workingHours = "24/7",
            description = "A private hospital offering premium healthcare services and surgeries.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Herzliya_Medical_Center.jpg/640px-Herzliya_Medical_Center.jpg",
            lat = 32.1665,
            lng = 34.8050
        ),
        Place(
            name = "Assuta Medical Center Tel Aviv",
            address = "HaBarzel St, Tel Aviv",
            city = "Tel Aviv",
            phone = "+972 3-7644444",
            workingHours = "24/7",
            description = "A private hospital with advanced surgical and diagnostic services.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/Assuta_Hospital_Tel_Aviv.jpg/640px-Assuta_Hospital_Tel_Aviv.jpg",
            lat = 32.1133,
            lng = 34.8387
        ),
        Place(
            name = "Yitzhak Shamir Medical Center (Asaf Harofeh)",
            address = "Tzrifin, Be'er Ya'akov",
            city = "Be'er Ya'akov",
            phone = "+972 8-9779999",
            workingHours = "24/7",
            description = "A central hospital formerly known as Assaf Harofeh, serving a wide urban and rural population.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7f/Assaf_Harofeh_Medical_Center.jpg/640px-Assaf_Harofeh_Medical_Center.jpg",
            lat = 31.9316,
            lng = 34.8371
        ),
        Place(
            name = "Assaf Harofeh (Shamir) Medical Center",
            address = "Tzrifin, near Rishon LeZion",
            city = "Be'er Ya'akov",
            phone = "+972 8-9779999",
            workingHours = "24/7",
            description = "One of the largest hospitals in Israel, renamed to Shamir Medical Center.",
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7f/Assaf_Harofeh_Medical_Center.jpg/640px-Assaf_Harofeh_Medical_Center.jpg",
            lat = 31.9316,
            lng = 34.8371
        )
    )
}
