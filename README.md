# Yörük
https://yoruk.benserhat.com/

[![Görsel Açıklaması](https://i.ytimg.com/vi/oy8H7u9mnFg/oar2.jpg?sqp=-oaymwEoCJUDENAFSFqQAgHyq4qpAxcIARUAAIhC2AEB4gEKCBgQAhgGOAFAAQ==&rs=AOn4CLChbCnOGAe5mrUzEoCpusVatZaAEQ&usqp=CCk)](https://www.youtube.com/shorts/oy8H7u9mnFg)

Yörük, seyahat planlamasını kolaylaştıran bir web uygulamasıdır.

Uygulama; ülke bilgileri, pasaport türüne göre vize gereksinimleri ve seyahat önerileri sunar.
Vize verileri açık kaynak sitelerden scrape edilerek güncellenir. Ayrıca Gemini API entegrasyonu sayesinde kullanıcıya seyahat edeceği ülkeye özel öneriler sağlanır.

## Özellikler

* Pasaport türüne göre vize bilgileri
* Ülke bazlı seyahat bilgileri
* Yapay zeka destekli seyahat önerileri
* Açık kaynak verilerden scraping sistemi
* Redis destekli önbellekleme yapısı
* Responsive modern arayüz

## Kullanılan Teknolojiler

### Backend

* Java Spring Boot
* PostgreSQL
* Redis

### Frontend

* Next.js
* TypeScript
* Tailwind CSS

### Servisler

* Gemini API

## Kurulum

### Backend

```bash id="o1u0wa"
git clone https://github.com/MetaMsa/yoruk-api
cd yoruk-api
setx DB_URL "your_url"
setx DB_USER "your_user"
setx DB_PASS "your_pass"

setx REDIS_URL "your_url"
```

Shell restart sonrası backend'i çalıştırın:

```bash id="38j6j6"
./mvnw spring-boot:run
```

### Frontend

```bash id="8qg5eq"
git clone https://github.com/MetaMsa/yoruk-frontend
cd yoruk-frontend
cp .env.example .env
npm install
npm run dev
```

## Kullanım

1. Gitmek istediğiniz ülkeyi seçin
2. Pasaport türünüzü belirleyin
3. Vize gereksinimlerini görüntüleyin
4. Yapay zeka destekli seyahat önerilerini inceleyin

## Uyarı

Vize bilgileri açık kaynak sitelerden elde edilmektedir.
Bilgiler güncel olmayabilir. Seyahat öncesinde resmi kaynakların kontrol edilmesi önerilir.

## Eklenecek Özellikler

* Ziyaret edilen ülkeleri kaydetme.

## Katkıda Bulunma

Her türlü katkıya açığız. Hataları bildirmek veya yeni özellik önerileri için pull request gönderebilirsiniz.

## Lisans

Bu proje MIT lisansı ile lisanslanmıştır.