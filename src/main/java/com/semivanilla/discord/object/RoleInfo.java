package com.semivanilla.discord.object;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Getter
@Setter
public class RoleInfo {
    private String name;
    private String roleId, guildId;
    private String description, buttonText, buttonColor;

    public void toggle(Member member, ButtonInteractionEvent event) {
        boolean hasRole = member.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));
        Role role = getRole(member);
        if (hasRole) {
            member.getGuild().removeRoleFromMember(member, role).queue();
        } else member.getGuild().addRoleToMember(member, role).queue();
        if (event != null) {
            if (!hasRole)
                event.getInteraction().reply("You now have the **" + role.getName() + "** role!").setEphemeral(true).queue();
            else
                event.getInteraction().reply("You no longer have the **" + role.getName() + "** role!").setEphemeral(true).queue();
        }
    }

    public Role getRole(Member member) {
        return member.getGuild().getRoleById(roleId);
    }
}
