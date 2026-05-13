-- Bu script, tc_no alanındaki boşlukları temizler ve olası tekrarları bulmanı kolaylaştırır.

-- 1) Baştaki/sondaki boşlukları temizle
UPDATE patients
SET tc_no = TRIM(tc_no)
WHERE tc_no <> TRIM(tc_no);

-- 2) Boşluk/format farkından kaynaklı tekrarları kontrol et
SELECT tc_no, COUNT(*) AS adet
FROM patients
GROUP BY tc_no
HAVING COUNT(*) > 1
ORDER BY adet DESC;
