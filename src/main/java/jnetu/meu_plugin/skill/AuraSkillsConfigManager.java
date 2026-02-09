package jnetu.meu_plugin.skill;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AuraSkillsConfigManager {

    private final Plugin plugin;
    private final File auraSkillsMenusDir;

    public AuraSkillsConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.auraSkillsMenusDir = new File(plugin.getServer().getPluginsFolder(), "AuraSkills/menus");
    }

    /**
     * Injeta as configurações customizadas nos menus do AuraSkills
     */
    public void injetarConfiguracoes() {
        if (!auraSkillsMenusDir.exists()) {
            plugin.getLogger().warning("Pasta de menus do AuraSkills não encontrada!");
            plugin.getLogger().warning("O AuraSkills precisa ter sido executado pelo menos uma vez.");
            return;
        }

        try {
            injetarCarismaNoStatInfo();
            injetarChatBatteryNoStatInfo();
            injetarSocialNoLevelProgression();
            plugin.getLogger().info("✅ Configurações injetadas nos menus do AuraSkills!");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao injetar configurações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Injeta a stat "Carisma" no arquivo stat_info.yml
     */
    private void injetarCarismaNoStatInfo() throws IOException {
        File statInfoFile = new File(auraSkillsMenusDir, "stat_info.yml");
        if (!statInfoFile.exists()) {
            plugin.getLogger().warning("stat_info.yml não encontrado!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(statInfoFile);

        // Verifica se já existe a configuração
        String basePathStat = "templates.stat.contexts";
        if (config.contains(basePathStat + ".meu_plugin:carisma")) {
            plugin.getLogger().info("Carisma já está configurado em stat_info.yml");
            return;
        }

        // Adiciona o contexto do Carisma
        config.set(basePathStat + ".carisma.material", "player_head");
        config.set(basePathStat + ".meu_plugin:carisma.material", "player_head");

        // Injeta o trait chat_battery
        String basePathTrait = "templates.trait.contexts";
        if (!config.contains(basePathTrait + ".meu_plugin:chat_battery")) {
            config.set(basePathTrait + ".chat_battery.material", "clock");
            config.set(basePathTrait + ".meu_plugin:chat_battery.material", "clock");
        }

        config.save(statInfoFile);
        plugin.getLogger().info("✓ Carisma e Chat Battery adicionados ao stat_info.yml");
    }

    /**
     * Injeta o trait "Chat Battery" no arquivo stat_info.yml (já feito acima)
     */
    private void injetarChatBatteryNoStatInfo() {
        // Já incluído no método injetarCarismaNoStatInfo()
    }

    /**
     * Injeta a skill "Social" no arquivo level_progression.yml
     */
    private void injetarSocialNoLevelProgression() throws IOException {
        File levelProgressionFile = new File(auraSkillsMenusDir, "level_progression.yml");
        if (!levelProgressionFile.exists()) {
            plugin.getLogger().warning("level_progression.yml não encontrado!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(levelProgressionFile);

        // Verifica se já existe
        String basePath = "templates.skill.contexts";
        if (config.contains(basePath + ".meu_plugin:social")) {
            plugin.getLogger().info("Social já está configurado em level_progression.yml");
            return;
        }

        // Adiciona o contexto da skill Social
        config.set(basePath + ".social.material", "player_head");
        config.set(basePath + ".meu_plugin:social.material", "player_head");

        config.save(levelProgressionFile);
        plugin.getLogger().info("✓ Skill Social adicionada ao level_progression.yml");
    }

    /**
     * Remove as injeções (para desinstalar o plugin limpo)
     */
    public void removerInjecoes() {
        try {
            removerDoArquivo("stat_info.yml", List.of(
                    "templates.stat.contexts.carisma",
                    "templates.stat.contexts.meu_plugin:carisma",
                    "templates.trait.contexts.chat_battery",
                    "templates.trait.contexts.meu_plugin:chat_battery"
            ));

            removerDoArquivo("level_progression.yml", List.of(
                    "templates.skill.contexts.social",
                    "templates.skill.contexts.meu_plugin:social"
            ));

            plugin.getLogger().info("✓ Injeções removidas dos menus do AuraSkills");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao remover injeções: " + e.getMessage());
        }
    }

    private void removerDoArquivo(String nomeArquivo, List<String> caminhos) throws IOException {
        File arquivo = new File(auraSkillsMenusDir, nomeArquivo);
        if (!arquivo.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(arquivo);

        for (String caminho : caminhos) {
            config.set(caminho, null);
        }

        config.save(arquivo);
    }
}
