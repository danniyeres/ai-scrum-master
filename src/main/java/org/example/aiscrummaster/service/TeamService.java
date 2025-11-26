package org.example.aiscrummaster.service;

import lombok.RequiredArgsConstructor;
import org.example.aiscrummaster.dto.TeamDto;
import org.example.aiscrummaster.dto.TeamMemberDto;
import org.example.aiscrummaster.model.Team;
import org.example.aiscrummaster.model.TeamMember;
import org.example.aiscrummaster.repository.TeamMemberRepository;
import org.example.aiscrummaster.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;


    public TeamDto createTeam(TeamDto teamDto) {
        Team team = Team.builder()
                .name(teamDto.getName())
                .build();

        teamRepository.save(team);

        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .teamMembers(team.getTeamMembers())
                .build();
    }

    public TeamDto createTeamWithMembers(String teamName, List<TeamMember> members) {
        Team team = new Team();
        team.setName(teamName);

        for (TeamMember member : members) {
            member.setTeam(team);
        }
        team.setTeamMembers(members);
        teamRepository.save(team);

        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .teamMembers(team.getTeamMembers())
                .build();
    }

    public TeamMemberDto addMemberToTeam(Long teamId, TeamMember newMember) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        newMember.setTeam(team);
        team.getTeamMembers().add(newMember);
        teamRepository.save(team);
        teamMemberRepository.save(newMember);

        return TeamMemberDto.builder()
                .id(newMember.getId())
                .fullName(newMember.getFullName())
                .email(newMember.getEmail())
                .telegramId(newMember.getTelegramId())
                .role(newMember.getRole())
                .team(team)
                .build();
    }

    public List<TeamDto> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream().map(team -> TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .teamMembers(team.getTeamMembers())
                .build()).toList();
    }

    public void removeMemberFromTeam(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Team Member not found"));
        Team team = member.getTeam();

        if (team != null) {
            team.getTeamMembers().remove(member);
            teamRepository.save(team);
        }
        teamMemberRepository.delete(member);
    }

    public List<TeamMemberDto> getAllTeamMembers() {
        List<TeamMember> members = teamMemberRepository.findAll();
        return members.stream().map(member -> TeamMemberDto.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .telegramId(member.getTelegramId())
                .role(member.getRole())
                .team(member.getTeam())
                .build()).toList();
    }
}