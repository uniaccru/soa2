// Конфиг для helios: запросы идут через PHP-мост api.php на том же сайте (https, без ssh-туннеля).
// USERNAME заменяет helios/upload.sh на логин.
window.CONFIG = {
  dragonServiceUrl: "/~USERNAME/soa2/api.php/dragon-service",
  killerServiceUrl: "/~USERNAME/soa2/api.php/killer-service"
};
